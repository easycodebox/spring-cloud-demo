package com.easycodebox.demo.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author WangXiaoJin
 * @date 2019-02-19 20:22
 */
@Data
public class Org implements Serializable {

    private static final long serialVersionUID = -627724321193105939L;

    private Long id;

    private String name;

    private List<Shop> shops;

}
