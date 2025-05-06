package com.fbytes.llmka.service.Maintenance;

public interface IMaintenanceService {
    void compressDB(String schema);
    void compressMeta(String schema, Integer size);
}
