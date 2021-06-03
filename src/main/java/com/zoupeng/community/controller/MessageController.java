package com.zoupeng.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.zoupeng.community.entity.Message;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.MessageService;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder holder;

    @Autowired
    UserService userService;

    //私信列表
    @RequestMapping(path = "/conversation/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = holder.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/conversation/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //查询会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        //Map是一个会话的信息，包括未读数量，消息总数，消息的所有属性
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> conversationVo = new HashMap<>();
                conversationVo.put("conversation", message);
                //当前会话未读消息数量
                conversationVo.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //当前会话总共消息数量，包括历史
                conversationVo.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                conversationVo.put("target", userService.findUserById(targetId));
                conversationVoList.add(conversationVo);
            }
        }
        model.addAttribute("conversationVoList", conversationVoList);
        //查询未读消息的数量
        int allUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("allUnreadCount", allUnreadCount);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    //获取某个会话的全部内容
    @RequestMapping(value = "/conversation/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //分页消息
        page.setLimit(5);
        page.setPath("/conversation/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //每一条Message对应一个发送人fromUser
        List<Map<String, Object>> letterVoList = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> letterVo = new HashMap<>();
                letterVo.put("letter", message);
                letterVo.put("fromUser", userService.findUserById(message.getFromId()));
                letterVoList.add(letterVo);
            }
        }
        model.addAttribute("letterVoList", letterVoList);
        //查询私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        //将消息设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                //我是接收者且消息是未读
                if (holder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    //获取目标用户，会话中，除自己外的就是会话目标
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (holder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else return userService.findUserById(id0);
    }


    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    //将消息发给谁
    public Map<String, String> sentLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        Map<String, String> map = new HashMap<>();
        if (target == null) {
            map.put("code", "1");
            map.put("msg", "目标用户不存在");
            return map;
        }
        Message message = new Message();
        message.setFromId(holder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < target.getId()) {
            message.setConversationId(message.getFromId() + "_" + target.getId());
        } else {
            message.setConversationId(target.getId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        map.put("code", "0");
        return map;
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = holder.getUser();

        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        messageVo.put("message", message);
        if (message != null) {
            //反转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unreadCount", unreadCount);
        }
        model.addAttribute("commentNotice", messageVo);
        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        messageVo.put("message", message);
        if (message != null) {
            //反转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unreadCount", unreadCount);
        }
        model.addAttribute("likeNotice", messageVo);
        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        messageVo.put("message", message);
        if (message != null) {
            //反转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unreadCount", unreadCount);
        }
        model.addAttribute("followNotice", messageVo);
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = holder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message message : noticeList) {
                Map<String, Object> noticeVo = new HashMap<>();
                //通知
                noticeVo.put("notice", message);
                //内容
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                noticeVo.put("user", userService.findUserById((Integer) data.get("userId")));
                noticeVo.put("entityType", data.get("entityType"));
                noticeVo.put("entityId", data.get("entityId"));
                noticeVo.put("postId", data.get("post_id"));
                //系统用户的名字
                noticeVo.put("fromUser", userService.findUserById(message.getFromId()));
                noticeVoList.add(noticeVo);
            }
            model.addAttribute("noticeVoList", noticeVoList);
        }
        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
