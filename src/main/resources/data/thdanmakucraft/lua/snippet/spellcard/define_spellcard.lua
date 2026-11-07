---@label 建立子彈
---@icon bossspellcard.png
---@description aa
---@parameter parameterName: 符卡變量名 = mySpellCard
---@parameter spellcardClassName: 符卡Class name = mySpellCard
---@parameter spellcardDisplayName: 符卡顯示名稱 = mySpellCard

local ${parameterName} = core.defineSpellCardClass("${spellcardClassName}")
function ${parameterName}:onInit()
    self:setSpellCardName("${spellcardDisplayName}")
    self:setMaxObjectAmount(600)
    self:setLifetime(120)
end

function ${parameterName}:onRegisterTasks(taskManager)

end

function ${parameterName}:onTick()

end