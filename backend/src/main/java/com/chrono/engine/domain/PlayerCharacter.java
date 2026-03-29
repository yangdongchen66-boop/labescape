package com.chrono.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家角色实体 (Player Character / PC)
 * 
 * 跑团游戏中的"车卡"，存储玩家的核心属性和状态。
 * 这是游戏状态机的核心组成部分，所有规则判定都基于这些属性。
 * 
 * D&D 5e 风格的六维属性系统：
 * - STR (力量): 影响物理攻击、负重
 * - DEX (敏捷): 影响闪避、先攻、远程攻击
 * - CON (体质): 影响生命值、毒素抗性
 * - INT (智力): 影响法术、知识检定
 * - WIS (感知): 影响洞察、侦查、意志
 * - CHA (魅力): 影响交涉、欺骗、领导力
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCharacter {

    /**
     * 角色唯一标识
     * 使用 UUID 生成，例如："pc-7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d"
     */
    private String id;

    /**
     * 角色名称
     * 玩家自定义的角色代号，例如："艾德里安"、"暗影之刃"
     */
    private String name;

    /**
     * 当前生命值 (HP)
     * 受到伤害时减少，归零时角色陷入昏迷/死亡
     */
    private int hp;

    /**
     * 最大生命值 (Max HP)
     * 由体质属性和职业决定，升级时增长
     */
    private int maxHp;

    /**
     * 当前魔力值/精神力 (MP/Sanity)
     * 施放法术或经历恐怖事件时消耗
     */
    private int mp;

    /**
     * 最大魔力值 (Max MP)
     * 由智力/感知属性决定
     */
    private int maxMp;

    /**
     * 六维属性映射表
     * Key: 属性缩写 (STR, DEX, CON, INT, WIS, CHA)
     * Value: 属性值 (3-18，人类平均 10)
     * 
     * 属性值转换修正值公式：(属性值 - 10) / 2 向下取整
     * 例如：18 -> +4, 14 -> +2, 10 -> 0, 8 -> -1
     */
    @Builder.Default
    private Map<String, Integer> attributes = new HashMap<>();

    /**
     * 便捷方法：获取属性修正值
     * 
     * @param attrName 属性名 (STR, DEX, CON, INT, WIS, CHA)
     * @return 修正值 (-5 到 +5)
     */
    public int getModifier(String attrName) {
        Integer attrValue = attributes.get(attrName);
        if (attrValue == null) {
            return 0;
        }
        return (int) Math.floor((attrValue - 10) / 2.0);
    }

    /**
     * 便捷方法：受到伤害
     * 
     * @param damage 伤害值
     * @return 实际受到的伤害（用于日志记录）
     */
    public int takeDamage(int damage) {
        int actualDamage = Math.max(0, damage);
        this.hp = Math.max(0, this.hp - actualDamage);
        return actualDamage;
    }

    /**
     * 便捷方法：恢复生命
     * 
     * @param amount 恢复量
     */
    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + Math.max(0, amount));
    }

    /**
     * 工厂方法：创建默认新手角色
     * 用于快速测试和演示
     * 
     * @param name 角色名
     * @return 预设属性的新手角色
     */
    public static PlayerCharacter createDefault(String name) {
        Map<String, Integer> defaultAttrs = new HashMap<>();
        defaultAttrs.put("STR", 14);  // 力量
        defaultAttrs.put("DEX", 16);  // 敏捷
        defaultAttrs.put("CON", 12);  // 体质
        defaultAttrs.put("INT", 10);  // 智力
        defaultAttrs.put("WIS", 13);  // 感知
        defaultAttrs.put("CHA", 15);  // 魅力

        return PlayerCharacter.builder()
                .id("pc-" + java.util.UUID.randomUUID().toString())
                .name(name)
                .hp(45)
                .maxHp(50)
                .mp(20)
                .maxMp(30)
                .attributes(defaultAttrs)
                .build();
    }
}
