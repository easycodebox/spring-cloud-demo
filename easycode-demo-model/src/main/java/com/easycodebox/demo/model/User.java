package com.easycodebox.demo.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author WangXiaoJin
 * @date 2019-02-19 20:22
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 5081839712212035130L;

    private Long id;

    private String name;

    private List<Org> orgs;

    private List<Shop> shops;

}
