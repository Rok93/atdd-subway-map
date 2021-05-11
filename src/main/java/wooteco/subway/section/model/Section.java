package wooteco.subway.section.model;

import java.util.Objects;
import wooteco.subway.exception.DuplicationException;
import wooteco.subway.exception.WrongDistanceException;
import wooteco.subway.line.model.Line;
import wooteco.subway.station.model.Station;

public class Section {

    private static final int MIN_DISTANCE_VALUE = 1;

    private final Line line;
    private final Station upStation;
    private final Station downStation;
    private final int distance;

    public Section(Line line, Station upStation, Station downStation, int distance) {
        validateSection(upStation, downStation, distance);
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    private void validateSection(Station upStation, Station downStation, int distance) {
        validateSameStations(upStation, downStation);
        validateDistance(distance);
    }

    private void validateSameStations(Station upStation, Station downStation) {
        if (upStation.equals(downStation)) {
            throw new DuplicationException("상행역과 하행역은 동일할 수 없습니다.");
        }
    }

    private void validateDistance(int distance) {
        if (distance < MIN_DISTANCE_VALUE) {
            throw new WrongDistanceException("거리는 1 이상이어야 합니다.");
        }
    }

    public boolean hasUpStation(Station newUpStation) {
        return upStation.equals(newUpStation);
    }

    public boolean hasDownStation(Station newDownStation) {
        return downStation.equals(newDownStation);
    }

    public boolean hasStationId(Long stationId) {
        return upStation.getId().equals(stationId) || downStation.getId().equals(stationId);
    }

    public Section splitByUpStation(Section newSection) {
        return new Section(this.line, newSection.getDownStation(), this.downStation,
            this.distance - newSection.getDistance());
    }

    public Section splitByDownStation(Section newSection) {
        return new Section(this.line, this.upStation, newSection.getUpStation(),
            this.distance - newSection.getDistance());
    }

    public Section merge(Section targetSection) {
        return new Section(this.line, this.upStation,
            targetSection.downStation, this.distance + targetSection.distance);
    }

    public Long getLineId() {
        return this.line.getId();
    }

    public Long getUpStationId() {
        return this.upStation.getId();
    }

    public Long getDownStationId() {
        return this.downStation.getId();
    }

    public Line getLine() {
        return line;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Section section = (Section) o;
        return distance == section.distance && Objects.equals(line, section.line)
            && Objects.equals(upStation, section.upStation) && Objects
            .equals(downStation, section.downStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, upStation, downStation, distance);
    }
}
