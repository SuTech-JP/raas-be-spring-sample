# raas-be-spring-sample

Spring Boot を使用した RaaS バックエンドサンプル。
[raas-client](https://github.com/SuTech-JP/raas-client)（`raas-spring-boot-starter`, Spring Boot向けSDK）を使用する。

## 動作環境

- Java 11+
- Maven 3.x

## セットアップ

### 1. GitHub Packages の認証を設定する

本サンプルは `raas-spring-boot-starter:1.0.12` を GitHub Packages
(`https://maven.pkg.github.com/SuTech-JP/raas-client`) から解決する（`pom.xml` に `<repositories>` 定義済み）。

GitHub Packages からの取得には認証が必要。`~/.m2/settings.xml` に `read:packages` スコープの
Personal Access Token を持つ `<server>` を設定すること（`<id>` は `pom.xml` のリポジトリ `<id>` と同じ `github`）。

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

### 2. 接続設定

以下のいずれかで RaaS 接続情報を設定する。
**実トークンを `application.yml` に直接書いてコミットしないこと**（このリポジトリは Public）。

**方法A: local プロファイル（ローカル開発ではこちらを推奨）:**

`src/main/resources/application-local.yml`（`.gitignore` 済み、コミットされない）を作成する。

```yaml
raas:
  application: your-application-name
  landscape: dev
  token: your-system-token
```

**方法B: 環境変数:**

```bash
export RAAS_APP=your-application-name
export RAAS_LANDSCAPE=dev          # dev または prod
export RAAS_TOKEN=your-system-token
export FRONTEND_ORIGIN=http://localhost:5173  # CORS 許可オリジン（任意）
```

### 3. アプリケーションの起動

```bash
# 方法A（application-local.yml を使う場合）
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run

# 方法B（環境変数を使う場合）
mvn spring-boot:run
```

デフォルトポート: **8080**（環境変数 `PORT` で変更可能）

## API エンドポイント

| Method | Path | 説明 | 実装 |
|--------|------|------|------|
| `GET`  | `/raas/health` | ヘルスチェック | `@EnableRaasProxy`自動登録 |
| `POST` | `/raas/report/session` | セッション発行（report） | `@EnableRaasProxy`自動登録 |
| `POST` | `/raas/datatraveler/session` | セッション発行（datatraveler） | `@EnableRaasProxy`自動登録 |
| `GET`  | `/raas/report/result/:targetId` | CSVインポートログ取得（statusがFINISHなら詳細データも取得してマージ） | `ImportLogController`（独自実装） |
| `GET`/`POST`/`PUT`/`DELETE` | `/raas/{*path}` | 汎用パススルー（例: `/raas/report/layouts/:application/:schema`） | `@EnableRaasProxy`自動登録 |

`@EnableRaasProxy(baseUrl = "/raas")` の汎用パススルーは1リクエストをそのまま転送するだけなので、
`/raas/report/result/:targetId`（条件分岐＋2回のAPI呼び出しをマージする処理）はこれだけでは実現できず、
`ImportLogController` として個別に実装している。

### エラーハンドリング

`RaasExceptionHandler`（`@RestControllerAdvice`）が以下をマッピングする。

| 例外 | レスポンス |
|---|---|
| RaaS側からの4xx（`HttpClientErrorException`） | 元のstatus codeをそのまま透過（例: 404 Not Found） |
| 不正なリクエストボディ（`HttpMessageNotReadableException`） | 400 |
| それ以外（リトライ全失敗など） | 500 |

いずれも `{"error": "..."}` 形式のJSONで返す。

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

**CSVインポートログ取得:**

```bash
curl http://localhost:8080/raas/report/result/0a996283-1444-4d61-8dd5-b9400faf38af
```

## プロジェクト構成

```
raas-be-spring-sample/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/jp/co/sutech/raas/sample/
    │   │   ├── RaasBeSpringSampleApplication.java  # @EnableRaasProxy(baseUrl = "/raas")
    │   │   ├── SampleSaasContextProvider.java       # テナント/ユーザーコンテキスト（サンプル用に固定値）
    │   │   ├── CorsConfig.java                      # CORS設定
    │   │   ├── ImportLogController.java             # CSVインポートログ取得（独自実装）
    │   │   └── RaasExceptionHandler.java            # エラーハンドリング
    │   └── resources/
    │       └── application.yml                      # RaaS接続情報・ポート
    └── test/java/jp/co/sutech/raas/sample/
        ├── ImportLogControllerTest.java
        └── RaasExceptionHandlerTest.java
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
