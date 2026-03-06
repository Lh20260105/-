/*
 Navicat Premium Dump SQL

 Source Server         : lost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : travel_system

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 04/03/2026 03:43:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attraction
-- ----------------------------
DROP TABLE IF EXISTS `attraction`;
CREATE TABLE `attraction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `open_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ticket_price` decimal(10, 2) NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `collect_count` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attraction
-- ----------------------------
INSERT INTO `attraction` VALUES (1, '故宫', '世界三大宫殿之一', '北京', '人文古迹', '08:30-17:00', 60.00, 'https://images.unsplash.com/photo-1547949003-9792a18a2601?w=800', 0, '2026-01-12 12:23:52');
INSERT INTO `attraction` VALUES (2, '长城', '不到长城非好汉', '北京', '自然风光', '08:00-18:00', 45.00, 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800', 0, '2026-01-12 12:59:56');
INSERT INTO `attraction` VALUES (4, '1', '是', '湖南', '自然风光', '11：00-18：00', 0.00, 'http://localhost:8080/files/1768295317837_ddd53ab890bcabd0b5ca5c9ecbb75abc8205301.gif', 0, '2026-01-13 17:09:03');

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轮播图标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片地址',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '', 'http://localhost:8080/files/1772562677337_46c31ca0.png', '', 1, 1, '2026-03-04 02:31:26');

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
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (10, 1, 3, '2026-01-12 20:32:05');

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_comment
-- ----------------------------
INSERT INTO `forum_comment` VALUES (1, 4, 2, 'saiki，移动', '2026-01-13 16:37:53');
INSERT INTO `forum_comment` VALUES (2, 4, 2, '做完啦', '2026-01-13 16:38:08');
INSERT INTO `forum_comment` VALUES (3, 4, 1, '6', '2026-01-13 16:54:50');
INSERT INTO `forum_comment` VALUES (4, 5, 2, '6666', '2026-01-13 17:25:46');
INSERT INTO `forum_comment` VALUES (5, 5, 3, '666', '2026-01-13 17:26:05');
INSERT INTO `forum_comment` VALUES (8, 5, 9, '123', '2026-03-04 02:53:23');
INSERT INTO `forum_comment` VALUES (9, 5, 9, '123', '2026-03-04 02:56:14');

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `itinerary_id` bigint NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `view_count` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  `user_id` bigint NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_date` date NULL DEFAULT NULL,
  `end_date` date NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of itinerary
-- ----------------------------
INSERT INTO `itinerary` VALUES (1, 1, '我的广州之旅', '2026-01-12', '2026-01-18', '2026-01-12 15:03:54');
INSERT INTO `itinerary` VALUES (2, 2, '我的旅行计划', '2026-01-12', NULL, '2026-01-12 18:48:37');
INSERT INTO `itinerary` VALUES (3, 9, '我的旅行计划', '2026-03-04', NULL, '2026-03-04 03:25:14');

-- ----------------------------
-- Table structure for itinerary_item
-- ----------------------------
DROP TABLE IF EXISTS `itinerary_item`;
CREATE TABLE `itinerary_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `itinerary_id` bigint NOT NULL,
  `attraction_id` bigint NOT NULL,
  `day_number` int NOT NULL,
  `start_time` time NOT NULL,
  `stay_days` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `itinerary_id`(`itinerary_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123', 'ADMIN', '2026-01-12 14:04:50', NULL, NULL);
INSERT INTO `user` VALUES (2, 'user1', '123', 'USER', '2026-01-12 14:04:50', 'http://localhost:8080/files/1768283256262_f74803d2fd1f4134bbea7757631f95cad0c85e95.jpg', '睦子米');
INSERT INTO `user` VALUES (3, '111', '111', 'USER', '2026-01-12 18:09:04', NULL, NULL);
INSERT INTO `user` VALUES (4, '12123', '12123', 'USER', '2026-03-02 00:06:00', NULL, NULL);
INSERT INTO `user` VALUES (5, '1111', '111', 'USER', '2026-03-02 03:40:26', NULL, NULL);
INSERT INTO `user` VALUES (6, '8888', '888888', 'USER', '2026-03-02 14:08:27', NULL, NULL);
INSERT INTO `user` VALUES (7, '112', '112233', 'USER', '2026-03-02 23:37:09', NULL, NULL);
INSERT INTO `user` VALUES (8, '123', '123', 'USER', '2026-03-03 13:04:47', NULL, NULL);
INSERT INTO `user` VALUES (9, '1234', '123', 'USER', '2026-03-03 14:19:03', 'http://localhost:8080/files/1772564539319_小春-113001465-0.png', NULL);

SET FOREIGN_KEY_CHECKS = 1;
