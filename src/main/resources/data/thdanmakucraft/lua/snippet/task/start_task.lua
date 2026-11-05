---@label 啟動任務
---@icon task_start.png
---@description 啟動已註冊的任務，lifetime 是持續 tick 數。
---@parameter name: 任務名稱 = myTask
---@parameter lifetime: 持續 tick 數 = 120
---@parameter delay: 推遲開始時間 tick 數 = 0

taskManager:startTask("${name}", ${lifetime}, ${delay})
