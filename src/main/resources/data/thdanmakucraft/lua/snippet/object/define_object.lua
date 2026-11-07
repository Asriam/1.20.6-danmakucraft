---@label 定義物件
---@icon objectdefine.png
---@description 宣告一個一般物件類別（THObject），可自行在 onInit / onTick 加入行為。
---@parameter parameterName: 子彈變量名 = laserClass

---@type Class|THObject
local ${parameterName} = core.defineObjectClass()
function ${parameterName}:onInit()

end

function ${parameterName}:onRegisterTasks(taskManager)

end

function ${parameterName}:onTick()

end