---@label 定義子彈
---@icon bulletdefine.png
---@description 宣告一個子彈類別（THBullet），onInit 設定初速、onTick 每 tick 更新。
---@parameter parameter_name: 子彈變量名 = bulletClass

local ${parameter_name} = core.defineClass()
function ${parameter_name}:onInit()

end

function ${parameter_name}:onRegisterTasks(taskManager)

end

function ${parameter_name}:onTick()

end