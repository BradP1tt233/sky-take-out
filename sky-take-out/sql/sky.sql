-- 根据数据库设计文档生成；逻辑外键不创建物理外键。
-- 文档未指定的非空约束与默认值不额外设置。
CREATE DATABASE IF NOT EXISTS sky CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sky;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `employee` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '姓名',
    `username` varchar(32) NULL COMMENT '用户名；唯一',
    `password` varchar(64) NULL COMMENT '密码',
    `phone` varchar(11) NULL COMMENT '手机号',
    `sex` varchar(2) NULL COMMENT '性别',
    `id_number` varchar(18) NULL COMMENT '身份证号',
    `status` int NULL COMMENT '账号状态；1正常 0锁定',
    `create_time` datetime NULL COMMENT '创建时间',
    `update_time` datetime NULL COMMENT '最后修改时间',
    `create_user` bigint NULL COMMENT '创建人id',
    `update_user` bigint NULL COMMENT '最后修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

CREATE TABLE IF NOT EXISTS `category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '分类名称；唯一',
    `type` int NULL COMMENT '分类类型；1菜品分类  2套餐分类',
    `sort` int NULL COMMENT '排序字段；用于分类数据的排序',
    `status` int NULL COMMENT '状态；1启用 0禁用',
    `create_time` datetime NULL COMMENT '创建时间',
    `update_time` datetime NULL COMMENT '最后修改时间',
    `create_user` bigint NULL COMMENT '创建人id',
    `update_user` bigint NULL COMMENT '最后修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

CREATE TABLE IF NOT EXISTS `dish` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '菜品名称；唯一',
    `category_id` bigint NULL COMMENT '分类id；逻辑外键',
    `price` decimal(10,2) NULL COMMENT '菜品价格',
    `image` varchar(255) NULL COMMENT '图片路径',
    `description` varchar(255) NULL COMMENT '菜品描述',
    `status` int NULL COMMENT '售卖状态；1起售 0停售',
    `create_time` datetime NULL COMMENT '创建时间',
    `update_time` datetime NULL COMMENT '最后修改时间',
    `create_user` bigint NULL COMMENT '创建人id',
    `update_user` bigint NULL COMMENT '最后修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dish_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS `dish_flavor` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `dish_id` bigint NULL COMMENT '菜品id；逻辑外键',
    `name` varchar(32) NULL COMMENT '口味名称',
    `value` varchar(255) NULL COMMENT '口味值',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品口味表';

CREATE TABLE IF NOT EXISTS `setmeal` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '套餐名称；唯一',
    `category_id` bigint NULL COMMENT '分类id；逻辑外键',
    `price` decimal(10,2) NULL COMMENT '套餐价格',
    `image` varchar(255) NULL COMMENT '图片路径',
    `description` varchar(255) NULL COMMENT '套餐描述',
    `status` int NULL COMMENT '售卖状态；1起售 0停售',
    `create_time` datetime NULL COMMENT '创建时间',
    `update_time` datetime NULL COMMENT '最后修改时间',
    `create_user` bigint NULL COMMENT '创建人id',
    `update_user` bigint NULL COMMENT '最后修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_setmeal_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

CREATE TABLE IF NOT EXISTS `setmeal_dish` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `setmeal_id` bigint NULL COMMENT '套餐id；逻辑外键',
    `dish_id` bigint NULL COMMENT '菜品id；逻辑外键',
    `name` varchar(32) NULL COMMENT '菜品名称；冗余字段',
    `price` decimal(10,2) NULL COMMENT '菜品单价；冗余字段',
    `copies` int NULL COMMENT '菜品份数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐菜品关系表';

CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `openid` varchar(45) NULL COMMENT '微信用户的唯一标识',
    `name` varchar(32) NULL COMMENT '用户姓名',
    `phone` varchar(11) NULL COMMENT '手机号',
    `sex` varchar(2) NULL COMMENT '性别',
    `id_number` varchar(18) NULL COMMENT '身份证号',
    `avatar` varchar(500) NULL COMMENT '微信用户头像路径',
    `create_time` datetime NULL COMMENT '注册时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `address_book` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `user_id` bigint NULL COMMENT '用户id；逻辑外键',
    `consignee` varchar(50) NULL COMMENT '收货人',
    `sex` varchar(2) NULL COMMENT '性别',
    `phone` varchar(11) NULL COMMENT '手机号',
    `province_code` varchar(12) NULL COMMENT '省份编码',
    `province_name` varchar(32) NULL COMMENT '省份名称',
    `city_code` varchar(12) NULL COMMENT '城市编码',
    `city_name` varchar(32) NULL COMMENT '城市名称',
    `district_code` varchar(12) NULL COMMENT '区县编码',
    `district_name` varchar(32) NULL COMMENT '区县名称',
    `detail` varchar(200) NULL COMMENT '详细地址信息；具体到门牌号',
    `label` varchar(100) NULL COMMENT '标签；公司、家、学校',
    `is_default` tinyint(1) NULL COMMENT '是否默认地址；1是 0否',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

CREATE TABLE IF NOT EXISTS `shopping_cart` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '商品名称',
    `image` varchar(255) NULL COMMENT '商品图片路径',
    `user_id` bigint NULL COMMENT '用户id；逻辑外键',
    `dish_id` bigint NULL COMMENT '菜品id；逻辑外键',
    `setmeal_id` bigint NULL COMMENT '套餐id；逻辑外键',
    `dish_flavor` varchar(50) NULL COMMENT '菜品口味',
    `number` int NULL COMMENT '商品数量',
    `amount` decimal(10,2) NULL COMMENT '商品单价',
    `create_time` datetime NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS `orders` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `number` varchar(50) NULL COMMENT '订单号',
    `status` int NULL COMMENT '订单状态；1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    `user_id` bigint NULL COMMENT '用户id；逻辑外键',
    `address_book_id` bigint NULL COMMENT '地址id；逻辑外键',
    `order_time` datetime NULL COMMENT '下单时间',
    `checkout_time` datetime NULL COMMENT '付款时间',
    `pay_method` int NULL COMMENT '支付方式；1微信支付 2支付宝支付',
    `pay_status` tinyint NULL COMMENT '支付状态；0未支付 1已支付 2退款',
    `amount` decimal(10,2) NULL COMMENT '订单金额',
    `remark` varchar(100) NULL COMMENT '备注信息',
    `phone` varchar(11) NULL COMMENT '手机号',
    `address` varchar(255) NULL COMMENT '详细地址信息',
    `user_name` varchar(32) NULL COMMENT '用户姓名',
    `consignee` varchar(32) NULL COMMENT '收货人',
    `cancel_reason` varchar(255) NULL COMMENT '订单取消原因',
    `rejection_reason` varchar(255) NULL COMMENT '拒单原因',
    `cancel_time` datetime NULL COMMENT '订单取消时间',
    `estimated_delivery_time` datetime NULL COMMENT '预计送达时间',
    `delivery_status` tinyint NULL COMMENT '配送状态；1立即送出  0选择具体时间',
    `delivery_time` datetime NULL COMMENT '送达时间',
    `pack_amount` int NULL COMMENT '打包费',
    `tableware_number` int NULL COMMENT '餐具数量',
    `tableware_status` tinyint NULL COMMENT '餐具数量状态；1按餐量提供  0选择具体数量',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键；自增',
    `name` varchar(32) NULL COMMENT '商品名称',
    `image` varchar(255) NULL COMMENT '商品图片路径',
    `order_id` bigint NULL COMMENT '订单id；逻辑外键',
    `dish_id` bigint NULL COMMENT '菜品id；逻辑外键',
    `setmeal_id` bigint NULL COMMENT '套餐id；逻辑外键',
    `dish_flavor` varchar(50) NULL COMMENT '菜品口味',
    `number` int NULL COMMENT '商品数量',
    `amount` decimal(10,2) NULL COMMENT '商品单价',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';