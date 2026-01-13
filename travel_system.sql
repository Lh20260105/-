/*
 Navicat Premium Dump SQL

 Source Server         : travel_db
 Source Server Type    : MySQL
 Source Server Version : 90500 (9.5.0)
 Source Host           : localhost:3306
 Source Schema         : travel_system

 Target Server Type    : MySQL
 Target Server Version : 90500 (9.5.0)
 File Encoding         : 65001

 Date: 13/01/2026 17:27:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attraction
-- ----------------------------
DROP TABLE IF EXISTS `attraction`;
CREATE TABLE `attraction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '景点名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '文字介绍',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地域',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
  `open_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开放时间',
  `ticket_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '门票价格',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `collect_count` int NULL DEFAULT 0 COMMENT '收藏次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attraction
-- ----------------------------
INSERT INTO `attraction` VALUES (1, '故宫', '世界三大宫殿之一', '北京', '人文古迹', '08:30-17:00', 60.00, 'https://images.unsplash.com/photo-1547949003-9792a18a2601?w=800', 0, '2026-01-12 12:23:52');
INSERT INTO `attraction` VALUES (2, '长城', '不到长城非好汉', '北京', '自然风光', '08:00-18:00', 45.00, 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800', 0, '2026-01-12 12:59:56');
INSERT INTO `attraction` VALUES (4, '1', '是', '湖南', '自然风光', '11：00-18：00', 0.00, 'http://localhost:8080/files/1768295317837_ddd53ab890bcabd0b5ca5c9ecbb75abc8205301.gif', 0, '2026-01-13 17:09:03');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `attraction_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (10, 1, 3, '2026-01-12 20:32:05');

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论人ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_comment
-- ----------------------------
INSERT INTO `forum_comment` VALUES (1, 4, 2, 'saiki，移动', '2026-01-13 16:37:53');
INSERT INTO `forum_comment` VALUES (2, 4, 2, '做完啦\n', '2026-01-13 16:38:08');
INSERT INTO `forum_comment` VALUES (3, 4, 1, '6', '2026-01-13 16:54:50');
INSERT INTO `forum_comment` VALUES (4, 5, 2, '6666', '2026-01-13 17:25:46');
INSERT INTO `forum_comment` VALUES (5, 5, 3, '666', '2026-01-13 17:26:05');

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `itinerary_id` bigint NULL DEFAULT NULL COMMENT '关联的行程单ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帖子内容',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `view_count` int NULL DEFAULT 0 COMMENT '阅读量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '论坛帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (5, 2, NULL, 'saki，移动', '123456', 'http://localhost:8080/files/1768296336344_f8553a50.jpg', 0, '2026-01-13 17:25:42');

-- ----------------------------
-- Table structure for itinerary
-- ----------------------------
DROP TABLE IF EXISTS `itinerary`;
CREATE TABLE `itinerary`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '行程标题',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of itinerary
-- ----------------------------
INSERT INTO `itinerary` VALUES (1, 1, '我的广州之旅', '2026-01-12', '2026-01-18', '2026-01-12 15:03:54');
INSERT INTO `itinerary` VALUES (2, 2, '我的旅行计划', '2026-01-12', NULL, '2026-01-12 18:48:37');

-- ----------------------------
-- Table structure for itinerary_item
-- ----------------------------
DROP TABLE IF EXISTS `itinerary_item`;
CREATE TABLE `itinerary_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `itinerary_id` bigint NOT NULL COMMENT '所属行程ID',
  `attraction_id` bigint NOT NULL COMMENT '景点ID',
  `day_number` int NOT NULL COMMENT '第几天（如第1天，第2天）',
  `start_time` time NOT NULL COMMENT '安排的开始时间（如 10:00:00）',
  `stay_days` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `itinerary_id`(`itinerary_id` ASC) USING BTREE,
  CONSTRAINT `itinerary_item_ibfk_1` FOREIGN KEY (`itinerary_id`) REFERENCES `itinerary` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of itinerary_item
-- ----------------------------
INSERT INTO `itinerary_item` VALUES (31, 2, 2, 1, '00:00:00', 5);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123', 'ADMIN', '2026-01-12 14:04:50', NULL, NULL);
INSERT INTO `user` VALUES (2, 'user1', '123', 'USER', '2026-01-12 14:04:50', 'http://localhost:8080/files/1768283256262_f74803d2fd1f4134bbea7757631f95cad0c85e95.jpg', '睦子米');
INSERT INTO `user` VALUES (3, '111', '111', 'USER', '2026-01-12 18:09:04', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
