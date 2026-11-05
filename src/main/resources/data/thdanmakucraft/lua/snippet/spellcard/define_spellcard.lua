---@label 建立子彈
---@icon bossspellcard.png
---@description aa
---@parameter spellcardClassName: 符卡Class name = mySpellCard
---@parameter spellcardDisplayName: 符卡顯示名稱 = mySpellCard"

local yukari_spellcrad_2 = core.defineSpellCardClass("${spellcardClassName}")
function yukari_spellcrad_2:onInit()
    self:setSpellCardName("${spellcardDisplayName}")
end

