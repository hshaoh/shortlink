package com.saas.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.shortlink.project.dao.entity.ShortLink;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /**
     * 短链接访问统计自增
     */
    @Update("""
            update t_link
            set 
                total_pv = total_pv + #{totalPv}, 
                total_uv = total_uv + #{totalUv}, 
                total_uip = total_uip + #{totalUip} 
            where 
                gid = #{gid} and full_short_url = #{fullShortUrl}
            """)
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );
}
