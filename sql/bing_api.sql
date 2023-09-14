/*
 Navicat Premium Data Transfer

 Source Server         : txdocker(124.221.102.169)
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : 124.221.102.169:3306
 Source Schema         : bing_api

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 13/09/2023 14:39:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alipay_info
-- ----------------------------
DROP TABLE IF EXISTS `alipay_info`;
CREATE TABLE `alipay_info`  (
  `orderNumber` bigint NOT NULL COMMENT '订单id',
  `subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易名称',
  `totalAmount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `buyerPayAmount` decimal(10, 2) NOT NULL COMMENT '买家付款金额',
  `buyerId` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '买家在支付宝的唯一id',
  `tradeNo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付宝交易凭证号',
  `tradeStatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易状态',
  `gmtPayment` datetime NOT NULL COMMENT '买家付款时间',
  `isDelete` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`orderNumber`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for api_order
-- ----------------------------
DROP TABLE IF EXISTS `api_order`;
CREATE TABLE `api_order`  (
  `id` bigint NOT NULL COMMENT '订单id',
  `userId` bigint NOT NULL COMMENT '用户id',
  `interfaceId` bigint NOT NULL COMMENT '接口id',
  `count` int NOT NULL COMMENT '购买数量',
  `totalAmount` decimal(10, 2) NOT NULL COMMENT '订单应付价格',
  `status` int NOT NULL DEFAULT 0 COMMENT '订单状态 0-未支付 1-已支付 2-超时支付 3-出库失败',
  `price` float(10, 2) NOT NULL COMMENT '单价（元/条）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for interface_info
-- ----------------------------
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名称',
  `description` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `url` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接口地址',
  `requestHeader` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求头',
  `responseHeader` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '响应头',
  `status` int NOT NULL DEFAULT 0 COMMENT '接口状态（0-关闭，1-开启）',
  `method` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '请求类型',
  `requestParams` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求参数',
  `responseParams` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '响应参数',
  `parameterExample` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '参数示例',
  `userId` bigint NOT NULL DEFAULT 1 COMMENT '创建人',
  `stock` int NOT NULL COMMENT '付费接口库存',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  `price` double NOT NULL DEFAULT 0 COMMENT '价格(元/条)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1008 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '接口信息' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userName` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAccount` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账号',
  `userAvatar` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'default.png' COMMENT '用户头像',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `userRole` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user / admin',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `userPassword` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `accessKey` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `secretKey` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int NOT NULL DEFAULT 1 COMMENT '0 - 封禁，1 - 正常',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uni_userAccount`(`userAccount` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for user_interface_info
-- ----------------------------
DROP TABLE IF EXISTS `user_interface_info`;
CREATE TABLE `user_interface_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint NOT NULL COMMENT '调用用户Id',
  `interfaceInfoId` bigint NOT NULL COMMENT '接口Id',
  `totalNum` int NOT NULL DEFAULT 0 COMMENT '总调用次数',
  `leftNum` int NULL DEFAULT 0 COMMENT '剩余调用次数',
  `status` int NOT NULL DEFAULT 0 COMMENT '0-正常 ，1-禁用',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户调用接口关系表' ROW_FORMAT = DYNAMIC;


SET FOREIGN_KEY_CHECKS = 1;
