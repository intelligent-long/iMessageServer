/*
 Navicat Premium Data Transfer

 Source Server         : LocalHost
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : imessage

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 05/02/2025 02:55:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for allow_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `allow_chat_message`;
CREATE TABLE `allow_chat_message` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `allow_voice` tinyint(1) NOT NULL DEFAULT '1',
  `allow_notice` tinyint(1) NOT NULL DEFAULT '1',
  UNIQUE KEY `ichat_id` (`imessage_id`,`channel_imessage_id`) USING BTREE,
  KEY `channel_ichat_id` (`channel_imessage_id`) USING BTREE,
  CONSTRAINT `allow_chat_message_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `allow_chat_message_ibfk_2` FOREIGN KEY (`channel_imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for amap_district_1st_level
-- ----------------------------
DROP TABLE IF EXISTS `amap_district_1st_level`;
CREATE TABLE `amap_district_1st_level` (
  `1st_level_adcode` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  PRIMARY KEY (`1st_level_adcode`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for amap_district_2nd_level
-- ----------------------------
DROP TABLE IF EXISTS `amap_district_2nd_level`;
CREATE TABLE `amap_district_2nd_level` (
  `2nd_level_adcode` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `1st_level_adcode` int NOT NULL,
  PRIMARY KEY (`2nd_level_adcode`) USING BTREE,
  KEY `country_adcode` (`1st_level_adcode`) USING BTREE,
  CONSTRAINT `amap_district_2nd_level_ibfk_1` FOREIGN KEY (`1st_level_adcode`) REFERENCES `amap_district_1st_level` (`1st_level_adcode`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for amap_district_3rd_level
-- ----------------------------
DROP TABLE IF EXISTS `amap_district_3rd_level`;
CREATE TABLE `amap_district_3rd_level` (
  `3rd_level_adcode` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `2nd_level_adcode` int NOT NULL,
  PRIMARY KEY (`3rd_level_adcode`) USING BTREE,
  KEY `province_adcode` (`2nd_level_adcode`) USING BTREE,
  CONSTRAINT `amap_district_3rd_level_ibfk_1` FOREIGN KEY (`2nd_level_adcode`) REFERENCES `amap_district_2nd_level` (`2nd_level_adcode`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for avatar
-- ----------------------------
DROP TABLE IF EXISTS `avatar`;
CREATE TABLE `avatar` (
  `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `data` longblob NOT NULL,
  `extension` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `time` datetime NOT NULL,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  CONSTRAINT `avatar_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast
-- ----------------------------
DROP TABLE IF EXISTS `broadcast`;
CREATE TABLE `broadcast` (
  `index` bigint NOT NULL AUTO_INCREMENT,
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `time` datetime NOT NULL,
  `last_edit_time` datetime DEFAULT NULL,
  `text` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`index`,`broadcast_id`) USING BTREE,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `broadcast_id` (`broadcast_id`) USING BTREE,
  CONSTRAINT `broadcast_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_channel_permission
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_channel_permission`;
CREATE TABLE `broadcast_channel_permission` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `permission` int NOT NULL,
  PRIMARY KEY (`imessage_id`) USING BTREE,
  CONSTRAINT `broadcast_channel_permission_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_channel_permission_exclude_connected_channels
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_channel_permission_exclude_connected_channels`;
CREATE TABLE `broadcast_channel_permission_exclude_connected_channels` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `channel_id` (`channel_id`) USING BTREE,
  CONSTRAINT `broadcast_channel_permission_exclude_connected_channels_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `broadcast_channel_permission_exclude_connected_channels_ibfk_2` FOREIGN KEY (`channel_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_comment
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_comment`;
CREATE TABLE `broadcast_comment` (
  `comment_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `to_comment_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `time` datetime NOT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`comment_id`) USING BTREE,
  KEY `broadcast_id` (`broadcast_id`) USING BTREE,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `to_comment_id` (`to_comment_id`) USING BTREE,
  CONSTRAINT `broadcast_comment_ibfk_1` FOREIGN KEY (`broadcast_id`) REFERENCES `broadcast` (`broadcast_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `broadcast_comment_ibfk_2` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `broadcast_comment_ibfk_3` FOREIGN KEY (`to_comment_id`) REFERENCES `broadcast_comment` (`comment_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_like
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_like`;
CREATE TABLE `broadcast_like` (
  `like_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `time` datetime NOT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`like_id`) USING BTREE,
  KEY `broadcast_id` (`broadcast_id`) USING BTREE,
  KEY `channel_id` (`imessage_id`) USING BTREE,
  CONSTRAINT `broadcast_like_ibfk_1` FOREIGN KEY (`broadcast_id`) REFERENCES `broadcast` (`broadcast_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `broadcast_like_ibfk_2` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_media
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_media`;
CREATE TABLE `broadcast_media` (
  `media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `media` longblob NOT NULL,
  `type` int NOT NULL,
  `extension` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `index` int NOT NULL,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `video_duration` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`media_id`) USING BTREE,
  KEY `broadcast_id` (`broadcast_id`) USING BTREE,
  CONSTRAINT `broadcast_media_ibfk_1` FOREIGN KEY (`broadcast_id`) REFERENCES `broadcast` (`broadcast_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_permission
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_permission`;
CREATE TABLE `broadcast_permission` (
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  PRIMARY KEY (`broadcast_id`) USING BTREE,
  CONSTRAINT `broadcast_permission_ibfk_1` FOREIGN KEY (`broadcast_id`) REFERENCES `broadcast` (`broadcast_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for broadcast_permission_exclude_connected_channels
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_permission_exclude_connected_channels`;
CREATE TABLE `broadcast_permission_exclude_connected_channels` (
  `broadcast_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  KEY `broadcast_id` (`broadcast_id`) USING BTREE,
  KEY `channel_id` (`channel_id`) USING BTREE,
  CONSTRAINT `broadcast_permission_exclude_connected_channels_ibfk_1` FOREIGN KEY (`broadcast_id`) REFERENCES `broadcast` (`broadcast_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `broadcast_permission_exclude_connected_channels_ibfk_2` FOREIGN KEY (`channel_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for channel_association
-- ----------------------------
DROP TABLE IF EXISTS `channel_association`;
CREATE TABLE `channel_association` (
  `association_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `is_requester` tinyint(1) NOT NULL,
  `request_time` datetime NOT NULL,
  `accept_time` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  PRIMARY KEY (`association_id`) USING BTREE,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `channel_ichat_id` (`channel_imessage_id`) USING BTREE,
  CONSTRAINT `channel_association_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `channel_association_ibfk_2` FOREIGN KEY (`channel_imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for channel_note
-- ----------------------------
DROP TABLE IF EXISTS `channel_note`;
CREATE TABLE `channel_note` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `channel_ichat_id` (`channel_imessage_id`) USING BTREE,
  CONSTRAINT `channel_note_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `channel_note_ibfk_2` FOREIGN KEY (`channel_imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for channel_tag
-- ----------------------------
DROP TABLE IF EXISTS `channel_tag`;
CREATE TABLE `channel_tag` (
  `tag_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `order` int NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  PRIMARY KEY (`tag_id`) USING BTREE,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  CONSTRAINT `channel_tag_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for channel_tag_channel
-- ----------------------------
DROP TABLE IF EXISTS `channel_tag_channel`;
CREATE TABLE `channel_tag_channel` (
  `tag_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  UNIQUE KEY `tag_id` (`tag_id`,`channel_imessage_id`) USING BTREE,
  KEY `ichat_id` (`channel_imessage_id`) USING BTREE,
  CONSTRAINT `channel_tag_channel_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `channel_tag` (`tag_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `channel_tag_channel_ibfk_2` FOREIGN KEY (`channel_imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `type` tinyint NOT NULL,
  `from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `to` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `time` datetime NOT NULL,
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `image` longblob,
  `file` longblob,
  `video` longblob,
  `voice` longblob,
  `unsend_message_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for exclude_broadcast_channel
-- ----------------------------
DROP TABLE IF EXISTS `exclude_broadcast_channel`;
CREATE TABLE `exclude_broadcast_channel` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `channel_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  KEY `ichat_id` (`imessage_id`) USING BTREE,
  KEY `channel_id` (`channel_id`) USING BTREE,
  CONSTRAINT `exclude_broadcast_channel_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exclude_broadcast_channel_ibfk_2` FOREIGN KEY (`channel_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `imessage_id_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `avatar_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `sex` tinyint DEFAULT NULL,
  `1st_level_adcode` int DEFAULT NULL,
  `2nd_level_adcode` int DEFAULT NULL,
  `3rd_level_adcode` int DEFAULT NULL,
  `register_time` datetime NOT NULL,
  `imessage_id_user_last_change_time` datetime DEFAULT NULL,
  PRIMARY KEY (`imessage_id`) USING BTREE,
  KEY `1st_level_adcode` (`1st_level_adcode`) USING BTREE,
  KEY `2nd_level_adcode` (`2nd_level_adcode`) USING BTREE,
  KEY `3rd_level_adcode` (`3rd_level_adcode`) USING BTREE,
  KEY `avatar_hash` (`avatar_hash`) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`1st_level_adcode`) REFERENCES `amap_district_1st_level` (`1st_level_adcode`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_ibfk_2` FOREIGN KEY (`2nd_level_adcode`) REFERENCES `amap_district_2nd_level` (`2nd_level_adcode`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_ibfk_3` FOREIGN KEY (`3rd_level_adcode`) REFERENCES `amap_district_3rd_level` (`3rd_level_adcode`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for user_profile_visibility
-- ----------------------------
DROP TABLE IF EXISTS `user_profile_visibility`;
CREATE TABLE `user_profile_visibility` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `email_visible` tinyint(1) NOT NULL,
  `sex_visible` tinyint(1) NOT NULL,
  `region_visible` tinyint(1) NOT NULL,
  UNIQUE KEY `ichat_id` (`imessage_id`) USING BTREE,
  CONSTRAINT `user_profile_visibility_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for ways_to_find_me
-- ----------------------------
DROP TABLE IF EXISTS `ways_to_find_me`;
CREATE TABLE `ways_to_find_me` (
  `imessage_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `by_imessage_id` tinyint(1) NOT NULL,
  `by_email` tinyint(1) NOT NULL,
  UNIQUE KEY `ichat_id` (`imessage_id`) USING BTREE,
  CONSTRAINT `ways_to_find_me_ibfk_1` FOREIGN KEY (`imessage_id`) REFERENCES `user` (`imessage_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
