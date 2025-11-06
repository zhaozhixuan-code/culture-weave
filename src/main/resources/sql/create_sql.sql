-- 创建数据库
create database if not exists 'culture_weave';


-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;


-- 非遗资源表
create table if not exists resources
(
    id             bigint auto_increment comment 'id' primary key,
    name           varchar(256)                       not null comment '资源名称',
    category       varchar(256)                       not null comment '分类',
    introduction   varchar(512)                       null comment '简介',
    resourceImgUrl varchar(1024)                      null comment '资源图片',
    tags           varchar(512)                       null comment '标签（JSON 数组）',
    region         varchar(256)                       null comment '地区',
    price          int                                null comment '授权价格',
    userId         bigint                             not null comment '创建用户 id',
    userName       varchar(256)                       null comment '创建用户姓名',
    editTime       datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 基于资源名称创建索引
    INDEX idx_category (category),         -- 基于资源类型创建索引
    INDEX idx_introduction (introduction), -- 基于简介创建索引
    INDEX idx_userId (userId)              -- 基于创建用户 id 创建索引
) comment '非遗资源' collate = utf8mb4_unicode_ci;


-- 用户二创表
create table if not exists creation
(
    id             bigint auto_increment comment 'id' primary key
)
