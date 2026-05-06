package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.UserTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.JwtUtil;
import com.mall.common.util.PasswordUtil;
import com.mall.dao.mapper.SysAdminMapper;
import com.mall.dao.mapper.SysRoleMapper;
import com.mall.dao.mapper.SysPermissionMapper;
import com.mall.model.entity.SysAdmin;
import com.mall.model.entity.SysRole;
import com.mall.model.entity.SysPermission;
import com.mall.model.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Platform admin service.
 * Handles platform admin login, CRUD, and role/permission management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAdminService {

    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final JwtUtil jwtUtil;

    /**
     * Platform admin login.
     */
    public LoginVO login(String username, String password) {
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, username));
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!PasswordUtil.verify(password, admin.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // Update last login time
        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminMapper.updateById(admin);

        String accessToken = jwtUtil.generateToken(admin.getId(), UserTypeEnum.PLATFORM.getCode(), 0L);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), UserTypeEnum.PLATFORM.getCode(), 0L);

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setUserId(admin.getId());
        userInfo.setUsername(admin.getUsername());
        userInfo.setNickname(admin.getRealName());
        userInfo.setUserType(UserTypeEnum.PLATFORM.getCode());
        userInfo.setTenantId(0L);
        loginVO.setUserInfo(userInfo);

        return loginVO;
    }

    /**
     * Create platform admin.
     */
    @Transactional(rollbackFor = Exception.class)
    public SysAdmin createAdmin(SysAdmin admin) {
        Long count = sysAdminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_EXISTS);
        }
        admin.setPassword(PasswordUtil.encode(admin.getPassword()));
        admin.setStatus(1);
        sysAdminMapper.insert(admin);
        return admin;
    }

    /**
     * Get admin list (paginated).
     */
    public PageResult<SysAdmin> getAdminList(int page, int limit) {
        Page<SysAdmin> pageParam = new Page<>(page, limit);
        Page<SysAdmin> result = sysAdminMapper.selectPage(pageParam,
                new LambdaQueryWrapper<SysAdmin>().orderByDesc(SysAdmin::getCreateTime));
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Update admin status.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminStatus(Long adminId, Integer status) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        admin.setStatus(status);
        sysAdminMapper.updateById(admin);
    }

    // ==================== Role CRUD ====================

    /**
     * Get all roles.
     */
    public List<SysRole> getRoleList() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getCreateTime));
    }

    /**
     * Create role.
     */
    @Transactional(rollbackFor = Exception.class)
    public SysRole createRole(SysRole role) {
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        role.setStatus(1);
        sysRoleMapper.insert(role);
        return role;
    }

    /**
     * Update role.
     */
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(Long roleId, SysRole role) {
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (role.getRoleName() != null) {
            existing.setRoleName(role.getRoleName());
        }
        if (role.getDescription() != null) {
            existing.setDescription(role.getDescription());
        }
        if (role.getStatus() != null) {
            existing.setStatus(role.getStatus());
        }
        sysRoleMapper.updateById(existing);
        return existing;
    }

    /**
     * Delete role.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        sysRoleMapper.deleteById(roleId);
    }

    // ==================== Permission ====================

    /**
     * Get permission list.
     */
    public List<SysPermission> getPermissionList() {
        return sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSortOrder));
    }

    /**
     * Get current admin info.
     */
    public LoginVO.UserInfo getInfo() {
        Long userId = com.mall.common.util.UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        SysAdmin admin = sysAdminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setUserId(admin.getId());
        userInfo.setUsername(admin.getUsername());
        userInfo.setNickname(admin.getRealName());
        userInfo.setAvatar("");
        userInfo.setUserType(com.mall.common.enums.UserTypeEnum.PLATFORM.getCode());
        userInfo.setTenantId(0L);
        
        return userInfo;
    }
}
