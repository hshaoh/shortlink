package com.saas.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.shortlink.project.dao.entity.LinkNetworkStats;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 访问网络监控持久层
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStats> {

    /**
     * 记录访问设备监控数据
     */
    @Insert("""
            INSERT INTO
            t_link_network_stats (full_short_url, date, cnt, network, create_time, update_time, del_flag)
            VALUES(#{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt};
            """)
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStats linkNetworkStats);


    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("""
            SELECT 
                tlns.network, 
                SUM(tlns.cnt) AS cnt 
            FROM 
                t_link tl INNER JOIN 
                t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url 
            WHERE 
                tlns.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0'
                AND tlns.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tlns.full_short_url, tl.gid, tlns.network;
            """)
    List<LinkNetworkStats> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内访问网络监控数据
     */
    @Select("""
            SELECT 
                tlns.network, 
                SUM(tlns.cnt) AS cnt 
            FROM 
                t_link tl INNER JOIN 
                t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlns.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tl.gid, tlns.network;
            """)
    List<LinkNetworkStats> listNetworkStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);
}
