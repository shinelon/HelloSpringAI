package com.shinelon.hello.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户实体
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String company;
    private List<Long> roleIds;
}
