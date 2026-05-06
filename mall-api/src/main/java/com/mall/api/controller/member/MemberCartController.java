package com.mall.api.controller.member;

import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.CartAddDTO;
import com.mall.model.vo.CartGroupVO;
import com.mall.service.order.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Member cart controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberCartController {

    private final CartService cartService;

    @PostMapping("/add")
    public R<Void> add(@Validated @RequestBody CartAddDTO dto) {
        Long memberId = UserContext.getUserId();
        cartService.addToCart(memberId, dto);
        return R.ok();
    }

    @GetMapping("/list")
    public R<List<CartGroupVO>> list() {
        Long memberId = UserContext.getUserId();
        return R.ok(cartService.getCartList(memberId));
    }

    @PutMapping("/item/{id}")
    public R<Void> updateItem(@PathVariable Long id, @RequestParam Integer quantity) {
        Long memberId = UserContext.getUserId();
        cartService.updateCartItem(memberId, id, quantity);
        return R.ok();
    }

    @DeleteMapping("/item/{id}")
    public R<Void> deleteItem(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        cartService.deleteCartItem(memberId, id);
        return R.ok();
    }
}
