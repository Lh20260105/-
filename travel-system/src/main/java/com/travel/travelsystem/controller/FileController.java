package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // 目标文件大小：2MB
    private static final long TARGET_SIZE = 2 * 1024 * 1024;
    
    // 使用项目根目录下的 files 文件夹
    private String getUploadPath() {
        String userDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(userDir, "files");
        return uploadDir.toString();
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 判断文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 2. 获取上传路径
            String uploadPath = getUploadPath();
            Path dirPath = Paths.get(uploadPath);
            
            // 3. 确保文件夹存在
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println(">>> [创建上传目录] " + dirPath.toAbsolutePath());
            }

            // 4. 获取原始文件名并提取后缀名
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            // 5. 生成唯一的文件名
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;
            File targetFile = new File(dirPath.toFile(), fileName);

            // 6. 检查是否是图片文件
            if (isImageFile(suffix)) {
                // 图片文件进行压缩处理
                byte[] compressedImage = compressImage(file, suffix);
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(compressedImage);
                }
            } else {
                // 非图片文件直接保存
                file.transferTo(targetFile);
            }

            // 7. 返回给前端访问的 URL 地址
            String url = "http://localhost:8080/files/" + fileName;

            System.out.println(">>> [文件上传成功] 访问路径: " + url + ", 文件大小: " + targetFile.length() / 1024 + "KB");
            return Result.success(url);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }

    /**
     * 判断是否是图片文件
     */
    private boolean isImageFile(String suffix) {
        return suffix.equals(".jpg") || suffix.equals(".jpeg") || 
               suffix.equals(".png") || suffix.equals(".webp") ||
               suffix.equals(".gif") || suffix.equals(".bmp");
    }

    /**
     * 压缩图片到目标大小（2MB）
     */
    private byte[] compressImage(MultipartFile file, String suffix) throws IOException {
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        // 如果文件已经小于2MB，直接返回原文件
        if (file.getSize() <= TARGET_SIZE) {
            return file.getBytes();
        }

        // 计算压缩比例
        double scale = Math.sqrt((double) TARGET_SIZE / file.getSize());
        
        // 如果压缩比例太激进，限制最小为0.1
        scale = Math.max(scale, 0.1);

        // 计算新的尺寸
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);
        
        // 确保最小尺寸
        newWidth = Math.max(newWidth, 100);
        newHeight = Math.max(newHeight, 100);

        // 创建压缩后的图片
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // 转换为字节数组，使用JPEG格式以获得更好的压缩率
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 根据原格式选择输出格式
        String formatName = getFormatName(suffix);
        
        if (formatName.equals("jpeg") || formatName.equals("jpg")) {
            // JPEG格式使用质量压缩
            compressJpeg(resizedImage, baos);
        } else {
            // 其他格式直接写入
            ImageIO.write(resizedImage, formatName, baos);
        }
        
        byte[] result = baos.toByteArray();
        
        // 如果还是太大，递归压缩
        if (result.length > TARGET_SIZE) {
            return recursiveCompress(resizedImage, formatName, 0.8f);
        }
        
        return result;
    }

    /**
     * 获取图片格式名称
     */
    private String getFormatName(String suffix) {
        switch (suffix.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "jpeg";
            case ".png":
                return "png";
            case ".webp":
                return "webp";
            case ".gif":
                return "gif";
            case ".bmp":
                return "bmp";
            default:
                return "jpeg";
        }
    }

    /**
     * 压缩JPEG图片
     */
    private void compressJpeg(BufferedImage image, OutputStream output) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.85f); // 85%质量
        
        ImageOutputStream ios = ImageIO.createImageOutputStream(output);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
        ios.close();
    }

    /**
     * 递归压缩直到达到目标大小
     */
    private byte[] recursiveCompress(BufferedImage image, String formatName, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (formatName.equals("jpeg") || formatName.equals("jpg")) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = writers.next();
            
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
        } else {
            // 对于非JPEG格式，缩小尺寸
            int newWidth = (int) (image.getWidth() * 0.9);
            int newHeight = (int) (image.getHeight() * 0.9);
            
            BufferedImage smallerImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = smallerImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            ImageIO.write(smallerImage, formatName, baos);
        }
        
        byte[] result = baos.toByteArray();
        
        // 如果还是太大且质量还可以降低，继续压缩
        if (result.length > TARGET_SIZE && quality > 0.3f) {
            return recursiveCompress(image, formatName, quality - 0.1f);
        }
        
        return result;
    }
}
