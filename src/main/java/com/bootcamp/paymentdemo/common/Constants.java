package com.bootcamp.paymentdemo.common;

public class Constants {

    private Constants() {
    }

    // region 인증 관련
    public static final String MSG_TOKEN_EMPTY = "인증 정보가 없습니다";
    public static final String MSG_TOKEN_EXPIRE = "인증 정보가 만료 되었습니다";
    public static final String MSG_AUTH_WRONG = "인증 정보가 올바르지 않습니다";
    public static final String MSG_AUTH_FAIL = "인증 처리가 실패 하였습니다";
    public static final String MSG_LOGOUT = "로그아웃 되었습니다";
    // endregion

    // region 도메인 관련 메세지
    // region 회원 관련 메세지
    public static final String MSG_NOT_FOUND_MEMBER = "회원을 찾을 수 없습니다";
    public static final String MSG_DUPLICATE_EMAIL = "중복된 이메일 입니다";
    public static final String MSG_NOT_MATCH_LOGIN = "아이디 또는 비밀번호가 일치하지 않습니다";
    // endregion

    // region 주문 관련 메세지
    public static final String MSG_NOT_FOUND_ORDER = "주문을 찾을 수 없습니다";
    public static final String MSG_NOT_FOUND_ORDER_PRODUCT = "주문하려는 상품을 찾을 수 없습니다";
    // endregion

    // region 상품 관련 메세지
    public static final String MSG_NOT_FOUND_PRODUCT = "상품을 찾을 수 없습니다";
    // endregion

    // region 결제 관련 메세지
    public static final String MSG_ALREADY_PAYMENT_COMPLETED = "이미 결제 확정 처리된 결제 건입니다";
    // endregion

    // region 환불 관련 메세지
    public static final String MSG_NOT_FOUND_PAYMENT = "존재하지 않는 결제 정보입니다";
    public static final String MSG_ALREADY_CANCELLED = "이미 환불로 취소된 결제입니다";
    public static final String MSG_INVALID_REFUND_STATUS = "환불 가능한 결제 상태가 아닙니다";
    public static final String MSG_FAIL_REFUND = "포트원 결제 취소 요청이 실패했습니다";
    // endregion

    // region 서버 관련 메세지
    public static final String MSG_NOT_VALID_VALUE = "유효하지 않은 값이 입력되었습니다";
    public static final String MSG_DATA_INSERT_FAIL = "데이터 등록에 실패하였습니다";
    public static final String MSG_SERVER_ERROR_OCCUR = "서버 오류가 발생하였습니다, 잠시 후 다시 시도 바랍니다";
    // endregion

    public static final String MSG_WEBHOOK_NOT_FOUND_PAYMENT = "포트원 결제 정보 조회 실패";
    public static final String MSG_WEBHOOK_INVALID_SIGNATURE = "웹훅 시그니처가 일치하지 않습니다";
}


