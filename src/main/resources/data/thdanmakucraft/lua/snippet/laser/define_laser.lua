---@label 定義雷射
---@icon laserdefine.png
---@description 宣告一個直線雷射類別（THLaser）並設定寬度與長度。
---@parameter parameterName: 子彈變量名 = laserClass
---@parameter width: 寬度 = 0.1
---@parameter length: 長度 = 40.0

---@type Class|THLaser
local ${parameterName} = core.defineLaserClass()
function ${parameterName}:onInit()
    self:setWidth(${width})
    self:setLength(${length})
end

function ${parameterName}:onRegisterTasks(taskManager)

end

function ${parameterName}:onTick()

end