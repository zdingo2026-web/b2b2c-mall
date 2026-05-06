package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberAddressMapper;
import com.mall.dao.mapper.MemberMapper;
import com.mall.model.entity.Member;
import com.mall.model.entity.MemberAddress;
import com.mall.model.vo.MemberAddressVO;
import com.mall.model.vo.MemberAssetVO;
import com.mall.model.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Member information service.
 * Handles member profile and address CRUD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberAddressMapper memberAddressMapper;
    private final MemberCouponService memberCouponService;

    /**
     * Get member info by ID.
     */
    public MemberVO getMemberInfo(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toMemberVO(member);
    }

    /**
     * Update member info.
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberVO updateMemberInfo(Long memberId, MemberVO dto) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (dto.getNickname() != null) {
            member.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            member.setAvatar(dto.getAvatar());
        }
        if (dto.getGender() != null) {
            member.setGender(dto.getGender());
        }
        if (dto.getEmail() != null) {
            // Check email uniqueness
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getEmail, dto.getEmail())
                            .ne(Member::getId, memberId));
            if (count > 0) {
                throw new BusinessException("邮箱已被使用");
            }
            member.setEmail(dto.getEmail());
        }

        memberMapper.updateById(member);
        return toMemberVO(member);
    }

    // ==================== Address CRUD ====================

    /**
     * Get member address list.
     */
    public List<MemberAddressVO> getAddressList(Long memberId) {
        List<MemberAddress> list = memberAddressMapper.selectList(
                new LambdaQueryWrapper<MemberAddress>()
                        .eq(MemberAddress::getMemberId, memberId)
                        .orderByDesc(MemberAddress::getIsDefault)
                        .orderByDesc(MemberAddress::getCreateTime));
        return list.stream().map(this::toAddressVO).collect(Collectors.toList());
    }

    /**
     * Get address by ID.
     */
    public MemberAddressVO getAddress(Long addressId, Long memberId) {
        MemberAddress address = memberAddressMapper.selectById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return toAddressVO(address);
    }

    /**
     * Add address.
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberAddressVO addAddress(Long memberId, MemberAddressVO dto) {
        MemberAddress address = new MemberAddress();
        address.setMemberId(memberId);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvinceId(dto.getProvinceId() != null ? dto.getProvinceId() : 0L);
        address.setCityId(dto.getCityId() != null ? dto.getCityId() : 0L);
        address.setDistrictId(dto.getDistrictId() != null ? dto.getDistrictId() : 0L);
        address.setProvinceName(dto.getProvinceName());
        address.setCityName(dto.getCityName());
        address.setDistrictName(dto.getDistrictName());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        address.setTag(dto.getTag());

        // If set as default, clear other defaults
        if (address.getIsDefault() == 1) {
            clearDefaultAddress(memberId);
        }

        memberAddressMapper.insert(address);
        return toAddressVO(address);
    }

    /**
     * Update address.
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberAddressVO updateAddress(Long addressId, Long memberId, MemberAddressVO dto) {
        MemberAddress address = memberAddressMapper.selectById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        if (dto.getReceiverName() != null) {
            address.setReceiverName(dto.getReceiverName());
        }
        if (dto.getReceiverPhone() != null) {
            address.setReceiverPhone(dto.getReceiverPhone());
        }
        if (dto.getProvinceId() != null) {
            address.setProvinceId(dto.getProvinceId());
        }
        if (dto.getCityId() != null) {
            address.setCityId(dto.getCityId());
        }
        if (dto.getDistrictId() != null) {
            address.setDistrictId(dto.getDistrictId());
        }
        if (dto.getProvinceName() != null) {
            address.setProvinceName(dto.getProvinceName());
        }
        if (dto.getCityName() != null) {
            address.setCityName(dto.getCityName());
        }
        if (dto.getDistrictName() != null) {
            address.setDistrictName(dto.getDistrictName());
        }
        if (dto.getDetailAddress() != null) {
            address.setDetailAddress(dto.getDetailAddress());
        }
        if (dto.getIsDefault() != null) {
            if (dto.getIsDefault() == 1) {
                clearDefaultAddress(memberId);
            }
            address.setIsDefault(dto.getIsDefault());
        }
        if (dto.getTag() != null) {
            address.setTag(dto.getTag());
        }

        memberAddressMapper.updateById(address);
        return toAddressVO(address);
    }

    /**
     * Delete address.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId, Long memberId) {
        MemberAddress address = memberAddressMapper.selectById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        memberAddressMapper.deleteById(addressId);
    }

    /**
     * Get member asset summary (balance, coupon count, points, red packet balance).
     *
     * @param memberId member ID
     * @return MemberAssetVO
     */
    public MemberAssetVO getMemberAssets(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        MemberAssetVO vo = new MemberAssetVO();
        vo.setBalance(member.getBalance() != null ? member.getBalance() : java.math.BigDecimal.ZERO);
        vo.setCouponCount((int) memberCouponService.getCouponCount(memberId));
        vo.setPoints(0);
        vo.setRedPacketBalance(java.math.BigDecimal.ZERO);
        return vo;
    }

    private void clearDefaultAddress(Long memberId) {
        MemberAddress update = new MemberAddress();
        update.setIsDefault(0);
        memberAddressMapper.update(update,
                new LambdaQueryWrapper<MemberAddress>()
                        .eq(MemberAddress::getMemberId, memberId)
                        .eq(MemberAddress::getIsDefault, 1));
    }

    private MemberVO toMemberVO(Member member) {
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setPhone(member.getPhone());
        vo.setNickname(member.getNickname());
        vo.setAvatar(member.getAvatar());
        vo.setGender(member.getGender());
        vo.setEmail(member.getEmail());
        vo.setStatus(member.getStatus());
        vo.setCreateTime(member.getCreateTime());
        // V2: extra member fields
        vo.setMemberLevel(1);
        vo.setPoints(0);
        vo.setRedPacketBalance(java.math.BigDecimal.ZERO);
        vo.setCouponCount((int) memberCouponService.getCouponCount(member.getId()));
        vo.setMemberNo("M" + member.getId());
        return vo;
    }

    private MemberAddressVO toAddressVO(MemberAddress address) {
        MemberAddressVO vo = new MemberAddressVO();
        vo.setId(address.getId());
        vo.setMemberId(address.getMemberId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvinceId(address.getProvinceId());
        vo.setCityId(address.getCityId());
        vo.setDistrictId(address.getDistrictId());
        vo.setProvinceName(address.getProvinceName());
        vo.setCityName(address.getCityName());
        vo.setDistrictName(address.getDistrictName());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault());
        vo.setTag(address.getTag());
        vo.setFullAddress(address.getProvinceName() + address.getCityName()
                + address.getDistrictName() + address.getDetailAddress());
        return vo;
    }
}
