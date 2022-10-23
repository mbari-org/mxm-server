package org.mbari.mxm.rest;

import org.mbari.mxm.db.mission.MissionStatusType;

public record MissionStatus(String missionId, MissionStatusType status) {}
