/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50737 (5.7.37-log)
 Source Host           : localhost:3306
 Source Schema         : xdcosv3_operations

 Target Server Type    : MySQL
 Target Server Version : 50737 (5.7.37-log)
 File Encoding         : 65001

 Date: 29/11/2022 17:33:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dcos_flow_option
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_flow_option`;
CREATE TABLE IF NOT EXISTS `dcos_flow_option`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `value` int(11) NULL DEFAULT NULL COMMENT '数字',
  `value_string` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数字含义',
  `created_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '添加人',
  `updated_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `created_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程操作表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_flow_option
-- ----------------------------
INSERT IGNORE INTO `dcos_flow_option` VALUES (1, 1, '同意', 'system', NULL, 1622450346195, NULL, '同意');
INSERT IGNORE INTO `dcos_flow_option` VALUES (2, 2, '退回', 'system', NULL, 1622450346195, NULL, '退回');
INSERT IGNORE INTO `dcos_flow_option` VALUES (3, 3, '提交', 'system', NULL, 1622450346195, NULL, '提交');
INSERT IGNORE INTO `dcos_flow_option` VALUES (4, 4, '取消', 'system', NULL, 1622450346195, NULL, '取消');
INSERT IGNORE INTO `dcos_flow_option` VALUES (5, 5, '后加签', 'system', NULL, 1622450346195, NULL, '后加签');
INSERT IGNORE INTO `dcos_flow_option` VALUES (6, 6, '前加签', 'system', NULL, 1622450346195, NULL, '前加签');
INSERT IGNORE INTO `dcos_flow_option` VALUES (999, -1, '未审核', 'system', NULL, 1622450346195, NULL, '未审核');

-- ----------------------------
-- Table structure for dcos_process
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_process`;
CREATE TABLE IF NOT EXISTS `dcos_process`  (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程名',
    `type` enum('NETWORK','EL','JF','PAS','TEST','TRANSFER','TOI') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '流程类型（\'NETWORK\':数通工单,\'EL\':机柜工单,\'JF\':跳纤工单,\'PAS\':增值服务工单,\'TEST\':测试工单,\'TRANSFER\':传输工单,\'TOI\':勘察工单）',
    `created_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '添加人',
    `updated_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
    `created_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
    `updated_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_unique`(`type`) USING BTREE COMMENT '唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_process
-- ----------------------------
INSERT IGNORE INTO `dcos_process` VALUES (1, '客户传输工单流程', 'TRANSFER', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (2, '客户机柜工单流程', 'EL', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (3, '客户跳纤工单流程', 'JF', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (4, '客户增值服务流程', 'PAS', 'admin', NULL, 1669778281127, NULL, NULL);
# INSERT IGNORE INTO `dcos_process` VALUES (5, '测试工单流程', 'TEST', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (6, '客户勘察工单流程', 'TOI', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (7, '客户数通工单流程', 'NETWORK', 'admin', NULL, 1669778281127, NULL, NULL);
INSERT IGNORE INTO `dcos_process` VALUES (8, '内部跳纤工单流程', 'IJF', 'admin', NULL, 1669778281127, NULL, NULL);


-- ----------------------------
-- Table structure for dcos_process_config
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_process_config`;
CREATE TABLE IF NOT EXISTS `dcos_process_config`  (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `process_id` bigint(20) NOT NULL COMMENT '流程主键',
    `act_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '流程定义ID',
    `version` int(10) NOT NULL COMMENT '版本',
    `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '流程文件名',
    `enabled` int(2) NOT NULL DEFAULT 0 COMMENT '是否生效(1：是，0：否)',
    `created_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '添加人',
    `updated_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
    `created_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
    `updated_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_process_id`(`process_id`) USING BTREE COMMENT '流程ID索引'
    ) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流程配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_process_config
-- ----------------------------

-- ----------------------------
-- Table structure for dcos_process_config_assignee
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_process_config_assignee`;
CREATE TABLE IF NOT EXISTS `dcos_process_config_assignee`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `config_id` bigint(20) NOT NULL COMMENT '流程配置ID',
  `assignee` bigint(20) NULL DEFAULT NULL COMMENT '处理人标识',
  `collections` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '组别名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_config_id`(`config_id`) USING BTREE COMMENT '流程配置ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流程配置 处理人信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_process_config_assignee
-- ----------------------------

-- ----------------------------
-- Table structure for dcos_process_config_variables
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_process_config_variables`;
CREATE TABLE IF NOT EXISTS `dcos_process_config_variables`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `config_id` bigint(20) NULL DEFAULT NULL COMMENT '流畅配置ID',
  `variable` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '变量名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_config_id`(`config_id`) USING BTREE COMMENT '流程配置ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流程配置-变量信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_process_config_variables
-- ----------------------------

-- ----------------------------
-- Table structure for dcos_request
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_request`;
CREATE TABLE IF NOT EXISTS `dcos_request`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `odd_number` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单号',
  `rule_id` bigint(20) NOT NULL COMMENT '单号规则ID',
  `process_id` bigint(20) NOT NULL COMMENT '流程ID',
  `proc_inst_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流程实例ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `config_version` int(10) NOT NULL COMMENT '流程配置版本号',
  `created_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '添加人',
  `updated_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `created_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_odd_number`(`odd_number`) USING BTREE COMMENT '唯一索引',
  INDEX `idx_process_id`(`process_id`) USING BTREE COMMENT '流程ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程表单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_request
-- ----------------------------

-- ----------------------------
-- Table structure for dcos_request_relation
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_request_relation`;
CREATE TABLE IF NOT EXISTS `dcos_request_relation`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `request_id` bigint(20) NULL DEFAULT NULL COMMENT '表单主键',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '表单父级ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_id`(`request_id`) USING BTREE COMMENT '表单主键ID索引',
  INDEX `idx_parent_id`(`parent_id`) USING BTREE COMMENT '表单父级主键ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '表单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_request_relation
-- ----------------------------

-- ----------------------------
-- Table structure for dcos_request_variables
-- ----------------------------
# DROP TABLE IF EXISTS `dcos_request_variables`;
CREATE TABLE IF NOT EXISTS `dcos_request_variables`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `request_id` bigint(20) NULL DEFAULT NULL COMMENT '表单ID',
  `variable` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '变量名',
  `value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '变量值',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_request_id_variable`(`request_id`, `variable`) USING BTREE COMMENT '表单-变量名索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '表单变量信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dcos_request_variables
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
