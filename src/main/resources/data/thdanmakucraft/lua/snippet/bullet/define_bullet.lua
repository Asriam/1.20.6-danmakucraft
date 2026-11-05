---@label 定義子彈
---@icon bulletdefine.png
---@description 宣告一個子彈類別（THBullet），onInit 設定初速、onTick 每 tick 更新。
---@parameter parameterName: 子彈變量名 = bulletClass

local ${parameterName} = core.defineClass()
--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onInit()

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 register_task_event.png ]]function ${parameterName}:onRegisterTasks(taskManager)

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onTick()

end--[[@SnippetEnd 114514]]