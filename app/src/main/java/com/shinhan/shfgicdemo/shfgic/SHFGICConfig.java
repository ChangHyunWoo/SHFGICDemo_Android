package com.shinhan.shfgicdemo.shfgic;

/**
 * 통합인증 연동 설정정보
 */
public class SHFGICConfig {
    private static final String TAG = SHFGICConfig.class.getName();


    //Log 설정
    public static final boolean isLogging = true;

    //서버 접속 URL
    public static final String SERVER_DOMAIN = "serverDomain";

    //fido 접속 정보
    public static final String SERVER_FIDO = "serverFido";
    public static final String FIDO_SITE_ID = "fidoSiteId";
    public static final String FIDO_SVC_ID = "fidoSvcId";

    //api url
    public static final String API_LIST_SHFGIC = "apiListSHFGIC";       //통합인증서 목록
    public static final String API_VERIFY_SHFGIC = "apiVerifySHFGIC";   //사용자 유효성 검증
    public static final String API_REQUEST_FIDO = "apiRequestFido";     //FIDO 요청
    public static final String API_CHECK_PIN_RULE = "apiCheckPinRule";  //PIN번호 유효성 확인
    public static final String API_REQUEST_SSO = "apiRequestSSO";


    /* parameter define */
    public static final String COMMAND = "command";                 //요청 구분
    public static final String BIZ_REQ_TYPE = "bizReqType";         //요청자 구분
    public static final String SITE_ID = "siteId";                  //사이트 식별자
    public static final String SVC_ID = "svcId";                    //서비스 식별자
    public static final String DEVICE_ID = "deviceId";              //단말 식별자
    public static final String APP_ID = "appId";                    //업무앱 식별자
    public static final String VERIFY_TYPE = "verifyType";          //인증장치타입
    public static final String CI_NO = "ciNo";                      //고객번호(CI)
    public static final String OS_TYPE = "osType";                  //OS구분
    public static final String PIN_PWD = "pinPwd";                  //간편비밀번호
    public static final String PIN_PWD_CHECK = "pinPwdCheck";       //간편비밀번호확인
    public static final String IC_ID = "icId";                      //통합ID
    public static final String REQUEST_TYPE = "requestType";        //업무 구분
    public static final String SVC_TRID = "svcTrId";                //QR svcTrid
    public static final String REAL_NM = "realNm";                  //고객실명
    public static final String IS_ALL = "isAll";                    //인증서 해지 포함여부
    public static final String TRANS_TYPE = "transType";            //발급타입
    public static final String SVC_TR_CHALLENGE = "svcTrChallenge"; //거래원문 해쉬값
    public static final String SRC_DOC = "srcDoc";                  //거래원문

    public static final String RESULT_CODE = "resultCode";          //결과 코드
    public static final String RESULT_MSG = "resultMsg";            //응답 메시지

    public static final String IC_DATA = "icData";                  //통합인증서 데이터
    public static final String STATE_CODE = "stateCode";            //상태코드
    public static final String EXPIRY_DATE = "expiryDate";          //만료일
    public static final String CNT_AUTH_FAIL = "cntAuthFail";       //인증실패횟수
    public static final String LOCK = "lock";                       //계정잠금여부
    public static final String AFFILIATES_CODE = "affiliatesCode";
    public static final String AFFILIATES_CODES = "affiliatesCodes";//그룹사 상태정보

    public static final String RESULT_DATA = "resultData";          //결과 데이터
    public static final String TR_ID = "trId";                      //거래ID
    public static final String TR_STATUS = "trStatus";              //거래상태
    public static final String TR_STATUS_MSG = "trStatusMsg";       //거래상태 메시지

    public static final String AAID_ALLOW_LIST = "aaidAllowList";   //허용AAID목록
    public static final String AAID = "aaid";                       //AAID

    public static final String SSO_DATA = "ssoData";



    //그룹사 요청
    public static final int REQUEST_IS_SHFGIC = 1001;       //통합인증서 유무
    public static final int REQUEST_LIST_SHFGIC = 1002;     //통합인증서 목록
    public static final int REQUEST_VERIFY_SHFGIC = 1003;   //사용자 유효성 검증
    public static final int REQUEST_SHFGIC_SSO = 1004;

    //기타 요청
    public static final int REQUEST_FIDO_SET_INIT = 1101;               //FIDO 초기셋팅
    public static final int REQUEST_FIDO_ALLOWED_AUTHNR = 1102;         //FIDO 허용단말 리스트 요청
    public static final int REQUEST_FIDO_CHECK_DEVICE = 1103;           //지원가능 단말여부 확인
    public static final int REQUEST_FIDO_CHECK_PIN_VALIDATION = 1104;   //PIN번호 유효성 확인

    //그룹사 요청(등록 요청)
    public static final int REQUEST_SHFGIC_REGIST = 1200;               //그룹사 요청(등록 요청)
    public static final int REQUEST_SHFGIC_REGIST_READY = 1201;         //FIDO 등록준비 요청
    public static final int REQUEST_SHFGIC_REGIST_MAIN = 1202;          //FIDO 등록 요청
    public static final int REQUEST_SHFGIC_REGIST_COMPLETE = 1203;      //FIDO 등록완료 요청

    //그룹사 요청(인증 요청)
    public static final int REQUEST_SHFGIC_AUTH = 1300;                 //그룹사 요청(인증 요청)
    public static final int REQUEST_SHFGIC_AUTH_READY = 1301;           //FIDO 인증준비 요청
    public static final int REQUEST_SHFGIC_AUTH_MAIN = 1302;            //FIDO 인증 요청
    public static final int REQUEST_SHFGIC_AUTH_COMPLETE = 1303;        //FIDO 인증완료 요청

    public static final int REQUEST_SHFGIC_AUTH_VERIFY = 1600;          //그룹사 요청(인증 요청) - 예외사항 : 로그인시 verify 체크 (지문인증)
    public static final int REQUEST_SHFGIC_AUTH_VERIFY_READY = 1601;    //FIDO 인증준비 요청 - 예외사항 : 로그인시 verify 체크 (지문인증)

    //그룹사 요청(해지/정지 요청)
    public static final int REQUEST_SHFGIC_UNREGIST = 1400;             //그룹사 요청(해지/정지 요청)
    public static final int REQUEST_SHFGIC_UNREGIST_READY = 1401;       //FIDO 해지/정지 준비 요청
    public static final int REQUEST_SHFGIC_UNREGIST_MAIN = 1402;        //FIDO 해지/정지 요청
    public static final int REQUEST_SHFGIC_UNREGIST_COMPLETE = 1403;    //FIDO 해지/정지 완료 요청

    //그룹사 요청(전자서명)
    public static final int REQUEST_SHFGIC_DIGITALSIGN = 1500;          //그룹사 요청(전자서명)
    public static final int REQUEST_SHFGIC_DIGITALSIGN_READY = 1501;    //FIDO 전자서명 준비 요청
    public static final int REQUEST_SHFGIC_DIGITALSIGN_MAIN = 1502;     //FIDO 전자서명 요청
    public static final int REQUEST_SHFGIC_DIGITALSIGN_COMPLETE = 1503; //FIDO 전자서명 완료 요청

    public static final int REQUEST_SHFGIC_VERIFY_SSO = 1700;
    public static final int REQUEST_SHFGIC_VERIFY_SSO_READY = 1701;
    public static final int REQUEST_SHFGIC_VERIFY_SSO_MAIN = 1702;
    public static final int REQUEST_SHFGIC_VERIFY_SSO_COMPLETE = 1703;

    //PcToApp 인증
    public static final int REQUEST_SHFGIC_AUTH_PCTOAPP = 1900;          //그룹사 요청(PcToApp 인증)
    public static final int REQUEST_SHFGIC_AUTH_PCTOAPP_READY = 1901;    //FIDO PcToApp 인증 준비 요청
    public static final int REQUEST_SHFGIC_AUTH_PCTOAPP_MAIN = 1902;     //FIDO PcToApp 인증 요청



    public static int getStartRequestKey(int requestKey) {
        return requestKey / 100 * 100;
    }

    public static int getReadyRequestKey(int requestKey) {
        return requestKey / 100 * 100 + 1;
    }

    public static int getMainRequestKey(int requestKey) {
        return requestKey / 100 * 100 + 2;
    }

    public static int getCompleteRequestKey(int requestKey) {
        return requestKey / 100 * 100 + 3;
    }

    //그룹사 코드
    public enum CodeGroupCode {
        BANK("001"),
        CARD("002"),
        INVESTMENT("003"),
        INSURANCE("004");

        private final String value;

        CodeGroupCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //FIDO 인증타입
    public enum CodeFidoVerifyType {
        PASSWORD("512"),
        FINGER("2");

        private final String value;

        CodeFidoVerifyType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //서버 요청 구분 코드
    public enum CodeServerCommand {
        ServiceRegist("requestServiceRegist"),      //FIDO 등록 준비요청
        ResultConfirm("trResultConfirm"),           //FIDO 등록 완료요청
        ServiceAuth("requestServiceAuth"),          //FIDO 인증 준비요청
        ServiceRelease("requestServiceAuth"),       //FIDO 해지 준비요청 (requestServiceRelease -> requestServiceAuth 로 변경)
        VerifySHFGIC("checkRegisteredStatus"),      //사용자 유효성 검증
        AllowedAuthnr("allowedAuthnr"),             //FIDO 허용단말 리스트 요청
        ServiceAuth2("requestServiceAuth2");        //FIDO 전자서명 준비요청

        private final String value;

        CodeServerCommand(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //서버 업무 구분 코드
    public enum CodeRequestType {

        REGIST_NEW("100"),              //가입
        REGIST_ADD("101"),              //등록
        REGIST_REENTRANCE("103"),       //재가입
        REGIST_REREGISTRATION("104"),   //재등록

        AUTH_FINGER("110"),             //지문추가를 위한 auth
        REGIST_FINGER("111"),           //지문추가를 위한 regi

        AUTH_LOGIN("200"),              //로그인
        AUTH_PCTOAPP("201"),            //PcToApp 인증
        AUTH_SSO("203"),

        TERMINATION_I("300"),           //통합인증수단 해지
        SUSPENSION_I("301"),            //통합인증수단 정지
        TERMINATION_O("302"),           //타인증수단 해지
        SUSPENSION_O("303"),            //타인증수단 정지

        INQUIRE_CERTI("400"),           //인증서조회

        E_SIGN("500"),                  //전자서명

        PASSWORD_AUTH("600"),           //타인증수단 사용자 검증, 통합인증 비밀번호 인증
        PASSWORD_RESET("601");          //비밀번호 변경

        private final String value;

        CodeRequestType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //인증서 상태 코드
    public enum CodeSHFGICState {
        NORMAL("1"),        //정상
        TERMINATION("2"),   //해지
        SUSPENSION("3");    //정지

        private final String value;

        CodeSHFGICState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //결과 코드
    public enum CodeResultCode {
        SUCCESS("000"),     //정상
        RP002("RP002"),	    //입력한 비밀번호와 등록된 비밀번호가 동일하지 않습니다. PIN등록-비밀번호와 등록된 비밀번호 확인 불일치
        AP001("AP001"),	    //비밀번호가 일치하지 않습니다. 입력한 비밀번호와 블록체인 비밀번호 불일치
        AS001("AS001"),	    //통합인증 상태를 확인하세요. 통합인증상태가 인증할 수 없는 상태, 인증서 유효여부 및 계정Lock여부
        F239("239"),        //FIDO - 디바이스에 지문이 전혀 등록되지 않은 경우
        F240("240"),        //FIDO - 단말의 등록지문정보 변경
        F101("101"),        //FIDO - 통합인증에 지문을 등록하지 않은 경우
        F9003("9003"),      //FIDO - 지문 취소버튼 이벤트
        FAIL("999");        //실패

        private final String value;

        CodeResultCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //거래상태
    public enum CodeTrStatus {
        FAIL("0"),          //거래실패
        COMPLETE("1"),	    //거래완료
        TIMEOUT("2"),	    //세션타임아웃
        PENDING("3"),	    //거래대기중 (앱실행안됨)
        NONE("9");          //거래없음

        private final String value;

        CodeTrStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}