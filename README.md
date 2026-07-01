# raas-be-spring-sample

Spring Boot を使用した RaaS バックエンドサンプル。
[raas-client](https://github.com/SuTech-JP/raas-client)（`raas-spring-boot-starter`, Spring Boot向けSDK）を使用する。

[raas-be-play-sample](https://github.com/SuTech-JP/raas-be-play-sample)（Play Framework + raas-client-java版）の Spring Boot 版に相当する。

## raas-be-play-sample との違い

`raas-client`（本サンプルが使うSDK）は `@EnableRaasProxy` を付けるだけで汎用リバースプロキシコントローラーが
自動登録されるため、`raas-be-play-sample` のような **独自コントローラーの実装が不要**。

また、`raas-client-java` にある `deleteTenant()` のような専用メソッドが `raas-client` には無く、
テナント削除APIの実パスが不明なため、本サンプルでは**テナント削除エンドポイントは未実装**。
実パスが判明すれば `raasClient.delete(path, params, clazz)` で実装できる。

## 動作環境

- Java 11+
- Maven 3.x

## セットアップ

### 1. raas-client を用意する

現時点で本サンプルが依存する `raas-spring-boot-starter:1.0.12-0` は pre-release（動作確認用）であり、
GitHub Packages にはまだ publish されていない。[raas-client](https://github.com/SuTech-JP/raas-client) を
チェックアウトし、ローカルの Maven リポジトリにインストールしておくこと。

```bash
cd raas-client
mvn install -DskipTests
```

正式リリース後は `pom.xml` の `raas-spring-boot-starter` のバージョンを正式版に切り替え、
GitHub Packages からの解決に切り替えること。

### 2. 接続設定

環境変数を設定するか、`src/main/resources/application.yml` を直接編集する。

**環境変数（推奨）:**

```bash
export RAAS_APP=your-application-name
export RAAS_LANDSCAPE=dev          # dev または prod
export RAAS_TOKEN=your-system-token
export FRONTEND_ORIGIN=http://localhost:5173  # CORS 許可オリジン（任意）
```

### 3. アプリケーションの起動

```bash
mvn spring-boot:run
```

デフォルトポート: **8080**（環境変数 `PORT` で変更可能）

## API エンドポイント

`@EnableRaasProxy(baseUrl = "/raas")` が自動登録する汎用リバースプロキシによるもの。

| Method | Path | 説明 |
|--------|------|------|
| `GET`  | `/raas/health` | ヘルスチェック |
| `POST` | `/raas/report/session` | セッション発行（report） |
| `POST` | `/raas/datatraveler/session` | セッション発行（datatraveler） |
| `GET`/`POST`/`PUT`/`DELETE` | `/raas/{*path}` | 汎用パススルー（例: `/raas/report/layouts/:application/:schema`, `/raas/datatraveler/import/logs/:targetId`） |

### リクエスト例

**セッション発行:**

```bash
curl -X POST http://localhost:8080/raas/report/session \
  -H "Content-Type: application/json" \
  -d '{"backUrl": "https://your-app.example.com", "subUrl": "", "subDomain": ""}'
```

**レイアウト一覧取得（汎用パススルー経由）:**

```bash
curl http://localhost:8080/raas/report/layouts/myapp/myschema
```

## プロジェクト構成

```
raas-be-spring-sample/
├── pom.xml
└── src/main/
    ├── java/jp/co/sutech/raas/sample/
    │   ├── RaasBeSpringSampleApplication.java  # @EnableRaasProxy(baseUrl = "/raas")
    │   ├── SampleSaasContextProvider.java       # テナント/ユーザーコンテキスト（サンプル用に固定値）
    │   └── CorsConfig.java                      # CORS設定
    └── resources/
        └── application.yml                      # RaaS接続情報・ポート
```

## 独自エンドポイントを追加する場合

`RaasClient` を `@Autowired` して独自の `@RestController` を書くこともできる（`raas-be-play-sample` の
`RaasController` と同じスタイル）。

```java
@Autowired
RaasClient raasClient;

// 汎用 GET
ResponseEntity<String> result = raasClient.get("/report/layouts/app/schema", String.class);

// 汎用 POST
ResponseEntity<String> created = raasClient.post("/some/endpoint", body, String.class);
```
