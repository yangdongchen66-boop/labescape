package com.chrono.engine.service;

import com.chrono.engine.domain.GameEvent;
import com.chrono.engine.domain.GameSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 事件系统 (Event System)
 * 
 * 管理游戏中的各种事件触发和处理
 * 包括随机事件、强制事件、阶段事件等
 * 
 * @author Chrono Engine Team
 * @since Phase 3
 */
@Slf4j
@Service
public class EventSystem {

    private final Random random = new Random();

    // 所有预定义事件
    private final List<GameEvent> predefinedEvents;

    public EventSystem() {
        // 初始化所有预定义事件
        predefinedEvents = Arrays.asList(
            GameEvent.mentorRaid(),
            GameEvent.hrContact(),
            GameEvent.powerOutage(),
            GameEvent.seniorAdvice(),
            GameEvent.weeklyMeeting(),
            GameEvent.deadlineReminder(),
            // 赚钱事件
            GameEvent.partTimeJob(),
            GameEvent.ghostwriting(),
            GameEvent.scholarshipArrival(),
            GameEvent.fatherTransfer()
        );
    }

    /**
     * 检查并触发事件
     * 
     * 每回合调用一次，检查是否有事件应该触发
     * 
     * @param session 游戏会话
     * @return 触发的事件列表（可能为空）
     */
    public List<GameEvent> checkAndTriggerEvents(GameSession session) {
        List<GameEvent> triggeredEvents = new ArrayList<>();
        
        // 1. 检查强制事件（优先级最高）
        for (GameEvent event : predefinedEvents) {
            if (event.isMandatory() && event.canTrigger(session) && !event.isTriggered()) {
                triggeredEvents.add(event);
                event.applyEffect(session);
                log.info("[Event] 强制事件触发: {}", event.getTitle());
            }
        }
        
        // 2. 检查随机事件（20%基础概率）
        if (random.nextInt(100) < 20) {
            List<GameEvent> availableEvents = predefinedEvents.stream()
                    .filter(e -> !e.isMandatory() && !e.isTriggered())
                    .filter(e -> e.canTrigger(session))
                    .toList();
            
            if (!availableEvents.isEmpty()) {
                // 随机选择一个事件
                GameEvent selectedEvent = availableEvents.get(
                    random.nextInt(availableEvents.size())
                );
                
                // 检查概率
                if (random.nextInt(100) < selectedEvent.getTriggerCondition().getProbability()) {
                    triggeredEvents.add(selectedEvent);
                    selectedEvent.applyEffect(session);
                    log.info("[Event] 随机事件触发: {}", selectedEvent.getTitle());
                }
            }
        }
        
        // 3. 检查风险相关事件
        if (session.getRisk() >= 70) {
            Optional<GameEvent> raidEvent = predefinedEvents.stream()
                    .filter(e -> e.getId().equals("mentor_raid"))
                    .filter(e -> !e.isTriggered())
                    .findFirst();
            
            if (raidEvent.isPresent() && random.nextInt(100) < 50) {
                GameEvent event = raidEvent.get();
                triggeredEvents.add(event);
                event.applyEffect(session);
                log.info("[Event] 高风险触发导师突袭: {}", event.getTitle());
            }
        }
        
        return triggeredEvents;
    }

    /**
     * 获取当前阶段应该触发的强制事件
     * 
     * @param session 游戏会话
     * @return 强制事件列表
     */
    public List<GameEvent> getPhaseMandatoryEvents(GameSession session) {
        List<GameEvent> mandatoryEvents = new ArrayList<>();
        String phase = session.getGamePhase();
        int day = session.getCurrentDay();
        
        switch (phase) {
            case "PREP":
                // 准备期：解锁公司流程
                if (day == 2) {
                    log.info("[Event] 准备期：解锁更多公司流程");
                }
                break;
                
            case "BREAKTHROUGH":
                // 突破期：推进到终面
                if (day == 3) {
                    log.info("[Event] 突破期：进入关键阶段");
                }
                break;
                
            case "FINAL":
                // 收官期：最后冲刺
                if (day == 6) {
                    log.info("[Event] 收官期：最终冲刺");
                }
                break;
        }
        
        return mandatoryEvents;
    }

    /**
     * 获取事件描述文本（用于显示给玩家）
     * 
     * @param event 事件
     * @return 格式化的描述
     */
    public String formatEventDescription(GameEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(event.getTitle()).append("】\n");
        sb.append(event.getDescription()).append("\n");
        
        // 添加效果描述
        GameEvent.EventEffect effect = event.getEffect();
        if (effect != null) {
            sb.append("\n效果：");
            if (effect.getHpChange() != 0) {
                sb.append(String.format(" HP%s%d", 
                    effect.getHpChange() > 0 ? "+" : "", effect.getHpChange()));
            }
            if (effect.getMpChange() != 0) {
                sb.append(String.format(" MP%s%d", 
                    effect.getMpChange() > 0 ? "+" : "", effect.getMpChange()));
            }
            if (effect.getRiskChange() != 0) {
                sb.append(String.format(" 风险%s%d", 
                    effect.getRiskChange() > 0 ? "+" : "", effect.getRiskChange()));
            }
            if (effect.getMentorMoodChange() != 0) {
                sb.append(String.format(" 导师态度%s%d", 
                    effect.getMentorMoodChange() > 0 ? "+" : "", effect.getMentorMoodChange()));
            }
            if (effect.getPreparationChange() != 0) {
                sb.append(String.format(" 准备度+%d", effect.getPreparationChange()));
            }
            if (effect.getGoldChange() != 0) {
                sb.append(String.format(" 金币%s%d", 
                    effect.getGoldChange() > 0 ? "+" : "", effect.getGoldChange()));
            }
        }
        
        return sb.toString();
    }

    /**
     * 重置事件状态（用于新的一天）
     * 
     * 注意：某些事件每天只能触发一次
     */
    public void resetDailyEvents() {
        // 可以在这里实现每日重置逻辑
        log.debug("[Event] 重置每日事件状态");
    }

    /**
     * 检查是否应该触发每天至少一个事件
     * 
     * @param session 游戏会话
     * @param todayEvents 今天已触发的事件数
     * @return 是否应该强制触发一个事件
     */
    public boolean shouldForceEvent(GameSession session, int todayEvents) {
        // 如果今天还没有触发任何事件，增加触发概率
        if (todayEvents == 0 && session.getTimeBlock() == 3) {
            // 深夜时如果还没事件，强制触发一个
            return random.nextInt(100) < 50;
        }
        return false;
    }
}
