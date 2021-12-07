local cursor = 0  
local resp = redis.call('SCAN',cursor,'MATCH',KEYS[1],'COUNT',10)
cursor = tonumber(resp[1])
local result = resp[2]
while(cursor ~= 0) do
    local resp1 = redis.call('SCAN',cursor,'MATCH',KEYS[1],'COUNT',10)
    cursor = tonumber(resp1[1])
    for key,value in pairs(resp1[2]) do
        table.insert(result,value)
    end
end
return result