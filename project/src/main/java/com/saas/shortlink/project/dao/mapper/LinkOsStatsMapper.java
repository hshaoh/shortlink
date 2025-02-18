package com.saas.shortlink.project.dao.mapper;


import com.saas.shortlink.project.dao.entity.LinkOsStats;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 操作系统统计访问持久层
 */
public interface LinkOsStatsMapper {

    /**
     * 记录地区访问监控数据
     */
    @Insert("""
            INSERT INTO
            t_link_os_stats (full_short_url, date, cnt, os, create_time, update_time, del_flag)
            VALUES(#{linkOsStats.fullShortUrl}, #{linkOsStats.date}, #{linkOsStats.cnt}, #{linkOsStats.os}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkOsStats.cnt};
            """)
    void shortLinkOsState(@Param("linkOsStats") LinkOsStats linkOsStats);

    /**
     * 根据短链接获取指定日期内操作系统监控数据
     */
    @Select("""
            SELECT 
                tlos.os, 
                SUM(tlos.cnt) AS count 
            FROM 
                t_link tl INNER JOIN 
                t_link_os_stats tlos ON tl.full_short_url = tlos.full_short_url 
            WHERE 
                tlos.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlos.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
            GROUP BY 
                tlos.full_short_url, tl.gid, tlos.os;
            """)
    List<HashMap<String, Object>> listOsStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内操作系统监控数据
     */
    @Select("""
            SELECT 
                tlos.os, 
                SUM(tlos.cnt) AS count 
            FROM 
                t_link tl INNER JOIN 
                t_link_os_stats tlos ON tl.full_short_url = tlos.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlos.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tl.gid, tlos.os;
            """)
    List<HashMap<String, Object>> listOsStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);
}

