package wooteco.subway.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.line.api.dto.LineResponse;

@DisplayName("노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    @DisplayName("노선을 생성한다.")
    @Test
    void createLine() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("color", "GREEN");
        params.put("name", "2호선");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    @DisplayName("노선 이름 중복 생성 불가 기능")
    @Test
    void duplicatedLineName() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("color", "GREEN");
        params.put("name", "2호선");

        RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        params.clear();
        params.put("color", "RED");
        params.put("name", "2호선");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.body().asString()).isEqualTo("이미 존재하는 노선 이름입니다.");
    }

    @DisplayName("잘못된 요청값으로 노선 생성 요청시, 예외처리")
    @Test
    void createLineFailByNotValidatedRequest() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("color", "");
        params.put("name", "2");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // then
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("color"))
                .isEqualTo("노선 색을 지정해야합니다."),
            () -> assertThat(response.body().jsonPath().getString("name"))
                .isEqualTo("노선 이름은 최소 2글자 이상만 가능합니다.")
        );
    }

    @DisplayName("노선 색깔 중복 생성 불가 기능")
    @Test
    void duplicatedLineColor() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("color", "GREEN");
        params.put("name", "2호선");

        RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        params.clear();
        params.put("color", "GREEN");
        params.put("name", "3호선");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.body().asString()).isEqualTo("이미 존재하는 노선 색깔입니다.");
    }


    @DisplayName("전체 노선을 조회한다")
    @Test
    void getLines() {
        //given
        Map<String, String> params = new HashMap<>();
        String name1 = "2호선";
        params.put("name", name1);
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        params.clear();
        String name2 = "신분당선";
        params.put("name", name2);
        params.put("color", "red");
        RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all();

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines")
            .then().log().all()
            .extract();

        //then
        List<String> expectedLineNames = Arrays.asList(name1, name2);
        List<String> resultLineNames = response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getName)
            .collect(Collectors.toList());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(resultLineNames).containsAll(expectedLineNames);
    }

    @DisplayName("단일 노선 조회")
    @Test
    void getLine() {
        //given
        Map<String, String> params = new HashMap<>();
        String name1 = "2호선";
        params.put("name", name1);
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        //when
        Long id = response1.jsonPath().getObject(".", LineResponse.class).getId();
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines/{lineId}", id)
            .then().log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        LineResponse lineResponse = response.jsonPath().getObject(".", LineResponse.class);
        assertThat(lineResponse.getName()).isEqualTo(name1);
        assertThat(lineResponse.getColor()).isEqualTo("green");
    }

    @DisplayName("노선을 수정하는 기능")
    @Test
    void updateLine() {
        //given
        Map<String, String> params = new HashMap<>();
        String name1 = "2호선";
        params.put("name", name1);
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        params.clear();
        String updatedName = "3호선";
        params.put("name", updatedName);
        params.put("color", "orange");

        //when
        Long id = response1.jsonPath().getObject(".", LineResponse.class).getId();
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/{lineId}", id)
            .then().log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("노선 삭제 기능")
    @Test
    void delete() {
        //given
        Map<String, String> params = new HashMap<>();
        String name1 = "2호선";
        params.put("name", name1);
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        //when
        Long id = response1.body().jsonPath().getObject(".", LineResponse.class).getId();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .delete("/lines/{lineId}", id)
            .then()
            .log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 단일 노선 조회")
    @Test
    void getLineIfNotFoundId() {
        //given
        Map<String, String> params = new HashMap<>();
        String name1 = "2호선";
        params.put("name", name1);
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        //when
        Long id = response1.jsonPath().getObject(".", LineResponse.class).getId();
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines/{lineId}", id + 1L)
            .then().log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.body().asString()).isEqualTo("존재하지 않는 노선 ID 입니다.");
    }

    @DisplayName("존재하지 않는 노선 삭제 요청 시, 예외 처리 기능")
    @Test
    void deleteIfNotExistLineId() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "green");
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        //when
        Long id = response1.body().jsonPath().getObject(".", LineResponse.class).getId();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .delete("/lines/{lineId}", id + 1)
            .then()
            .log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.body().asString()).isEqualTo("존재하지 않는 노선 ID 입니다.");
    }
}
