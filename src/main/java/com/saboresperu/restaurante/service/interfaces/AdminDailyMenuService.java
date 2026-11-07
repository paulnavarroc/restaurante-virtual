package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.*;

import java.util.List;

public interface AdminDailyMenuService {
    List<DailyMenuRowDto> listAll();
    DailyMenuDetailDto getDetail(Integer id);
    Integer createDraft(DailyMenuCreateRequest request);
    void setItemShifts(Integer dailyMenuId, Integer itemId, List<Integer> branchShiftIds);
    List<BranchShiftDto> listBranchShifts(Integer branchId);
    void addItem(Integer dailyMenuId, DailyMenuItemRequest request);
    void updateItem(Integer dailyMenuId, Integer itemId, DailyMenuItemRequest request);
    void removeItem(Integer dailyMenuId, Integer itemId);
    void publish(Integer dailyMenuId, Integer userId);
    void close(Integer dailyMenuId);
}