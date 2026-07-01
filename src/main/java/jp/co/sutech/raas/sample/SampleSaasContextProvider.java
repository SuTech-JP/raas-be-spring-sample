package jp.co.sutech.raas.sample;

import jp.co.sutech.raas.SaasContextProvider;
import org.springframework.stereotype.Component;

/**
 * サンプル用のテナント/ユーザーコンテキスト。
 * 実際の実装ではリクエストの認証情報（JWTなど）から取得すること。
 */
@Component
public class SampleSaasContextProvider implements SaasContextProvider {
    @Override
    public String getTenant() {
        return "sample_tenant";
    }

    @Override
    public String getSub() {
        return "sample_user";
    }

    @Override
    public String getTenantAlias() {
        return null;
    }

    @Override
    public String getSubAlias() {
        return null;
    }

    @Override
    public String getSubDomain() {
        return null;
    }
}
