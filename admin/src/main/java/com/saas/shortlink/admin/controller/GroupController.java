package com.saas.shortlink.admin.controller;

import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.common.convention.result.Results;
import com.saas.shortlink.admin.dto.GroupSaveDTO;
import com.saas.shortlink.admin.dto.GroupSortDTO;
import com.saas.shortlink.admin.dto.GroupUpdateDTO;
import com.saas.shortlink.admin.service.GroupService;
import com.saas.shortlink.admin.vo.GroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> save(@RequestBody GroupSaveDTO groupSaveDTO) {
        groupService.saveGroup(groupSaveDTO.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组集合
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<GroupVO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody GroupUpdateDTO groupUpdateDTO) {
        groupService.updateGroup(groupUpdateDTO);
        return Results.success();
    }

    /**
     * 删除短链接分组
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 排序短链接分组
     */
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortDTO> groupSortDTOList) {
        groupService.sortGroup(groupSortDTOList);
        return Results.success();
    }

}
