package com.zzx.cultureweavebackend.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ai对话信息存储表
 * @TableName chat_message
 */
@TableName(value = "chat_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 对话数据
     */
    private String messageData;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     * 0-未删除
     * 1-已删除
     */
    private Integer isDelete;
}