package org.umc.travlocksserver.domain.member.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.umc.travlocksserver.domain.member.enums.ConsentStatus;

import java.util.List;

public record MemberSignupRequestDTO(
        @NotBlank(message = "signupToken은 필수입니다.")
        String signupToken,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바르지 않은 이메일 형식입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[A-Za-z])[A-Za-z\\d]{8,20}$",
                message = "비밀번호는 영문+숫자 조합이고 숫자를 반드시 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z]{2,10}$",
                message = "닉네임은 한글/영문만 가능합니다."
        )
        String nickname,

        @NotNull(message = "약관 동의 목록은 필수입니다.")
        @Size(min = 1, message = "빈 약관 동의 목록을 허용하지 않습니다.")
        @Valid
        List<ConsentDTO> consents,

        // 선택
        List<Long> preferredTravelStyleIds,
        List<Long> preferredTravelThemeIds
) {
    public record ConsentDTO(
            @NotNull(message = "policyId는 필수입니다.")
            Long policyId,

            @NotNull(message = "status는 필수입니다.")
            ConsentStatus status
    ) {}
}
