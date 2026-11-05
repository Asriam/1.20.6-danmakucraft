---@label 定義彎曲雷射
---@icon laserbentcreate.png
---@description 宣告一個彎曲雷射類別（THCurvedLaser），可設定寬度。
---@parameter parameterName: 子彈變量名 = laserClass
---@parameter width: 寬度 = 0.8

---@type Class|THCurvedLaser
local ${parameterName} = core.defineClass()
--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onInit()
    self:setWidth(${width})
end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 register_task_event.png ]]function ${parameterName}:onRegisterTasks(taskManager)

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onTick()

end--[[@SnippetEnd 114514]]