---@label 定義物件
---@icon objectdefine.png
---@description 宣告一個一般物件類別（THObject），可自行在 onInit / onTick 加入行為。
---@parameter parameterName: 子彈變量名 = laserClass

---@type Class|THObject
local objectClass = core.defineClass()
--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onInit()

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 register_task_event.png ]]function ${parameterName}:onRegisterTasks(taskManager)

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onTick()

end--[[@SnippetEnd 114514]]