---@label 註冊任務
---@icon taskdefine.png
---@description 向 taskManager 註冊一個每 tick 執行的任務（target 是持有人）。
---@parameter name: 任務名稱 = myTask

taskManager:registerTask("${name}", function(target, timer, lifetime)
    -- 每個 tick 執行
end)