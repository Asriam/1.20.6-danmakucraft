---@label 建立子彈
---@icon bossspellcard.png
---@description aa
---@parameter parameterName: 符卡變量名 = mySpellCard
---@parameter spellcardClassName: 符卡Class name = mySpellCard
---@parameter spellcardDisplayName: 符卡顯示名稱 = mySpellCard

local ${parameterName} = core.defineSpellCardClass("${spellcardClassName}")
--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onInit()
    self:setSpellCardName("${spellcardDisplayName}")
    self:setMaxObjectAmount(600)
    self:setLifetime(120)
end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 register_task_event.png ]]function ${parameterName}:onRegisterTasks(taskManager)

end--[[@SnippetEnd 114514]]

--[[@SnippetStart 114514 event.png ]]function ${parameterName}:onTick()

end--[[@SnippetEnd 114514]]