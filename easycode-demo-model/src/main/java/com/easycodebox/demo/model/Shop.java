package com.easycodebox.demo.model;

import java.io.Serializable;
import lombok.Data;

/**
 * @author WangXiaoJin
 * @date 2019-02-19 20:22
 */
@Data
public class Shop implements Serializable {

    private static final long serialVersionUID = -8832500700940151950L;

    private Long id;

    private String name;

}
