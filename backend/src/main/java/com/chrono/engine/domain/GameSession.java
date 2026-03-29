package com.chrono.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏会话实体 (Game Session)
 * 
 * 代表一局完整的跑团游戏对局，是状态机的顶层容器。
 * 包含玩家角色、对话历史、当前场景等核心上下文。
 * 
 * 生命周期：
 * 1. CREATED (创建)：会话初始化，等待玩家连接
 * 2. ACTIVE (进行中)：正常游戏流程，接受玩家输入
 * 3. PAUSED (暂停)：玩家暂离或战斗轮等待
 * 4. ENDED (结束)：故事完结或玩家退出
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {

    /**
     * 会话唯一标识
     * 使用 UUID 生成，用于前端 SSE 连接时的会话追踪
     */
    private String sessionId;

    /**
     * 关联的玩家角色
     * 当前 Phase 1 仅支持单人模式，后续可扩展为多人派对
     */
    private PlayerCharacter player;

    /**
     * 对话历史记录
     * 存储所有 DM 旁白、玩家输入、NPC 回复的摘要
     * 用于 Agent 上下文理解和"记忆"功能
     * 
     * 格式示例：
     * - "[DM] 晚上10点，实验室里只有机箱的嗡鸣..."
     * - "[PLAYER] 我要和导师谈谈实习的事"
     * - "[NPC:王导] 那个横向项目的接口写完没？"
     */
    @Builder.Default
    private List<String> chatHistory = new ArrayList<>();

    /**
     * 当前场景标识
     * 例如："lab" (实验室)、"office" (导师办公室)、"dorm" (宿舍)
     * 用于 Executor 加载对应的场景 Prompt
     */
    private String currentScene;

    /**
     * 会话状态
     * 控制游戏流程的状态机
     */
    @Builder.Default
    private SessionStatus status = SessionStatus.CREATED;

    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 最后活跃时间
     * 用于会话超时检测和清理
     */
    @Builder.Default
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    /**
     * NPC 记忆状态映射表
     * Key: NPC名称 (如 "王导", "李师弟")
     * Value: NPC对该玩家的态度值 (-50~+50，负值敌对，正值友好)
     * 
     * 用于保持 NPC 人设一致性，让 NPC "记得" 之前的态度
     */
    @Builder.Default
    private java.util.Map<String, Integer> npcAttitudes = new java.util.HashMap<>();

    /**
     * 玩家行为历史记录
     * 记录玩家对各个 NPC 的主要行为类型，用于 NPC 反应生成
     * 
     * 格式示例：
     * - "王导:顺从" (玩家选择了顺从/道歉)
     * - "王导:反抗" (玩家选择了硬刚/拒绝)
     * - "王导:谈判" (玩家选择了说服/沟通)
     */
    @Builder.Default
    private java.util.List<String> playerBehaviorHistory = new java.util.ArrayList<>();

    // ==================== 游戏核心状态（Phase 3新增）====================

    /**
     * 游戏阶段
     * PREP: 准备期 (Day 1-2)
     * BREAKTHROUGH: 突破期 (Day 3-5)
     * FINAL: 收官期 (Day 6-7)
     */
    @Builder.Default
    private String gamePhase = "PREP";

    /**
     * 风险值 0-100
     * 影响检定惩罚，>=70触发导师突袭事件
     */
    @Builder.Default
    private int risk = 0;

    /**
     * 导师态度 -100~100
     * 影响检定修正和剧情走向
     */
    @Builder.Default
    private int mentorMood = -10;

    /**
     * 准备度 0-5
     * 提供检定加成
     */
    @Builder.Default
    private int preparation = 0;

    /**
     * 当前游戏天数 1-7
     */
    @Builder.Default
    private int currentDay = 1;

    /**
     * 时间块 0-3
     * 0: 上午, 1: 下午, 2: 晚上, 3: 深夜
     */
    @Builder.Default
    private int timeBlock = 2;  // 默认从晚上开始

    /**
     * 游戏内小时数 0-168
     */
    @Builder.Default
    private int inGameHours = 22;  // 默认晚上10点

    /**
     * Offer流程追踪
     */
    @Builder.Default
    private java.util.List<CompanyProgress> companyProgresses = new java.util.ArrayList<>();

    /**
     * 主线任务状态
     * Key: 任务ID, Value: 是否完成
     */
    @Builder.Default
    private java.util.Map<String, Boolean> mainQuests = new java.util.HashMap<>();

    /**
     * 子任务列表
     */
    @Builder.Default
    private java.util.List<SideQuest> sideQuests = new java.util.ArrayList<>();

    /**
     * 最后处理请求的时间戳（毫秒）
     * 用于防止重复处理同一请求
     */
    private Long lastProcessTime;

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        /** 会话已创建，等待初始化 */
        CREATED,
        /** 游戏进行中，正常接受输入 */
        ACTIVE,
        /** 暂停状态（战斗轮等待、玩家暂离） */
        PAUSED,
        /** 会话已结束 */
        ENDED
    }

    /**
     * 添加对话记录并更新时间戳
     * 
     * @param entry 对话条目
     */
    public void addChatEntry(String entry) {
        this.chatHistory.add(entry);
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 添加 NPC 回复到对话历史
     * 
     * @param npcName NPC名称
     * @param content NPC回复内容
     */
    public void addNpcResponse(String npcName, String content) {
        this.chatHistory.add("[NPC:" + npcName + "] " + content);
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 更新 NPC 对玩家的态度
     * 
     * @param npcName NPC名称
     * @param delta 态度变化值（正数增加好感，负数降低）
     * @return 更新后的态度值
     */
    public int updateNpcAttitude(String npcName, int delta) {
        int current = this.npcAttitudes.getOrDefault(npcName, 0);
        int updated = Math.max(-50, Math.min(50, current + delta)); // 限制在 -50~+50
        this.npcAttitudes.put(npcName, updated);
        this.lastActiveAt = LocalDateTime.now();
        return updated;
    }

    /**
     * 获取 NPC 对玩家的态度
     * 
     * @param npcName NPC名称
     * @return 态度值 (-50~+50)
     */
    public int getNpcAttitude(String npcName) {
        return this.npcAttitudes.getOrDefault(npcName, 0);
    }

    /**
     * 记录玩家对 NPC 的行为
     * 
     * @param npcName NPC名称
     * @param behavior 行为类型 (顺从/反抗/谈判/欺骗/帮助/忽视)
     */
    public void recordPlayerBehavior(String npcName, String behavior) {
        this.playerBehaviorHistory.add(npcName + ":" + behavior);
        // 只保留最近20条行为记录
        if (this.playerBehaviorHistory.size() > 20) {
            this.playerBehaviorHistory.remove(0);
        }
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 获取玩家对特定 NPC 的行为模式
     * 
     * @param npcName NPC名称
     * @return 该 NPC 相关的行为记录列表
     */
    public java.util.List<String> getBehaviorPattern(String npcName) {
        return this.playerBehaviorHistory.stream()
            .filter(b -> b.startsWith(npcName + ":"))
            .toList();
    }

    /**
     * 获取最近的对话历史（用于上下文窗口）
     * 
     * @param limit 返回条数上限
     * @return 最近的对话记录列表
     */
    public List<String> getRecentHistory(int limit) {
        int size = chatHistory.size();
        if (size <= limit) {
            return new ArrayList<>(chatHistory);
        }
        return new ArrayList<>(chatHistory.subList(size - limit, size));
    }

    /**
     * 更新会话状态
     * 
     * @param newStatus 新状态
     */
    public void updateStatus(SessionStatus newStatus) {
        this.status = newStatus;
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 检查会话是否活跃
     * 
     * @return true 如果状态为 ACTIVE
     */
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    /**
     * 工厂方法：创建新会话
     * 
     * @param playerName 玩家角色名
     * @return 初始化好的游戏会话
     */
    public static GameSession createNew(String playerName) {
        GameSession session = GameSession.builder()
                .sessionId("sess-" + java.util.UUID.randomUUID().toString())
                .player(PlayerCharacter.createDefault(playerName))
                .currentScene("lab")  // 默认起始场景：实验室
                .status(SessionStatus.ACTIVE)
                .gamePhase("PREP")
                .risk(0)
                .mentorMood(-10)
                .preparation(0)
                .currentDay(1)
                .timeBlock(2)  // 晚上
                .inGameHours(22)  // 晚上10点
                .build();
        
        // 初始化 NPC 态度（王导初始态度-10，因为玩家还没完成横向项目）
        session.npcAttitudes.put("王导", -10);
        session.npcAttitudes.put("李师弟", 5);  // 李师弟初始友好
        
        // 添加初始场景描述到历史
        session.chatHistory.add("[DM] 晚上10点，实验室里只有机箱的嗡鸣。左边屏幕是跑不通的多智能体算法，右边是阿里和CNCERT的内推确认邮件（距截止仅剩7天）。门突然开了，王导夹着公文包走进来，脸色铁青：那个横向项目的接口写完没？周末加个班。 他拉开椅子坐在你旁边。");
        
        // 初始化主线任务
        session.mainQuests.put("pass_written_test", false);
        session.mainQuests.put("pass_interview", false);
        session.mainQuests.put("get_offer", false);
        session.mainQuests.put("mentor_approval", false);
        
        // 初始化子任务
        session.sideQuests.add(SideQuest.createStudyQuest("study_1", "刷题准备笔试", 3));
        session.sideQuests.add(SideQuest.createSocialQuest("social_1", "与师兄交流获取信息"));
        session.sideQuests.add(SideQuest.builder()
                .id("risk_control")
                .description("控制risk < 50")
                .type("prep")
                .requiredProgress(1)
                .rewardPrep(1)
                .build());
        
        // 初始化公司进度（Day 1先解锁一家）
        session.companyProgresses.add(CompanyProgress.createNew("阿里", 15));
        
        return session;
    }
}
