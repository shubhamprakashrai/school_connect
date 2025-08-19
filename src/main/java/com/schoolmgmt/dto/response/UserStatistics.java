package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User statistics")
public class UserStatistics {
    
    @Schema(description = "Total users", example = "150")
    private long totalUsers;
    
    @Schema(description = "Active users", example = "140")
    private long activeUsers;
    
    @Schema(description = "Total teachers", example = "20")
    private long teachers;
    
    @Schema(description = "Total students", example = "100")
    private long students;
    
    @Schema(description = "Total parents", example = "20")
    private long parents;
}
