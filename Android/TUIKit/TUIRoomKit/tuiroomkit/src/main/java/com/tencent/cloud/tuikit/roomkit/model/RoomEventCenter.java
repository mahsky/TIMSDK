package com.tencent.cloud.tuikit.roomkit.model;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.interfaces.ITUINotification;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomEventCenter {
    private static final String TAG = "RoomEventCenter";

    private Map<RoomEngineEvent, List<RoomEngineEventResponder>> mEngineResponderMap;
    private Map<String, List<TUINotificationAdapter>>            mUIEventResponderMap;

    /**
     * 事件列表
     */
    public enum RoomEngineEvent {
        ERROR,
        KICKED_OFF_LINE,
        USER_SIG_EXPIRED,
        ROOM_NAME_CHANGED,
        LOCAL_CAMERA_STATE_CHANGED,
        LOCAL_SCREEN_STATE_CHANGED,
        LOCAL_AUDIO_STATE_CHANGED,
        LOCAL_AUDIO_ROUTE_CHANGED,
        ALL_USER_MICROPHONE_DISABLE_CHANGED,
        ALL_USER_CAMERA_DISABLE_CHANGED,
        SEND_MESSAGE_FOR_ALL_USER_DISABLE_CHANGED,
        ROOM_DISMISSED,
        KICKED_OUT_OF_ROOM,
        ROOM_SPEECH_MODE_CHANGED,
        GET_USER_LIST_COMPLETED_FOR_ENTER_ROOM,
        REMOTE_USER_ENTER_ROOM,
        REMOTE_USER_LEAVE_ROOM,
        LOCAL_USER_CREATE_ROOM,
        LOCAL_USER_ENTER_ROOM,
        LOCAL_USER_EXIT_ROOM,
        LOCAL_USER_DESTROY_ROOM,
        USER_ROLE_CHANGED,
        USER_SCREEN_STATE_CHANGED,
        USER_CAMERA_STATE_CHANGED,
        USER_MIC_STATE_CHANGED,
        USER_VOICE_VOLUME_CHANGED,
        USER_SEND_MESSAGE_ABILITY_CHANGED,
        USER_NETWORK_QUALITY_CHANGED,
        USER_SCREEN_CAPTURE_STOPPED,
        ROOM_MAX_SEAT_COUNT_CHANGED,
        REMOTE_USER_TAKE_SEAT,
        REMOTE_USER_LEAVE_SEAT,
        REQUEST_RECEIVED,
        REQUEST_CANCELLED,
        RECEIVE_TEXT_MESSAGE,
        RECEIVE_CUSTOM_MESSAGE,
        KICKED_OFF_SEAT,
        USER_TAKE_SEAT_REQUEST_ADD,
        USER_TAKE_SEAT_REQUEST_REMOVE,
        LOCAL_VIDEO_FPS_CHANGED,
        LOCAL_VIDEO_BITRATE_CHANGED,
        LOCAL_VIDEO_RESOLUTION_CHANGED,
        LOCAL_AUDIO_CAPTURE_VOLUME_CHANGED,
        LOCAL_AUDIO_PLAY_VOLUME_CHANGED,
        LOCAL_AUDIO_VOLUME_EVALUATION_CHANGED,
    }

    public static class RoomKitUIEvent {
        public static final String ROOM_KIT_EVENT        = "RoomKitEvent";
        public static final String CONFIGURATION_CHANGE  = "appConfigurationChange";
        public static final String KICKED_OFF_LINE       = "kickedOffLine";
        public static final String AGREE_TAKE_SEAT       = "agreeTakeSeat";
        public static final String DISAGREE_TAKE_SEAT    = "disagreeTakeSeat";
        public static final String INVITE_TAKE_SEAT      = "inviteTakeSeat";
        public static final String SHOW_USER_MANAGEMENT  = "showUserManagement";
        public static final String SHOW_EXIT_ROOM_VIEW   = "showLeaveRoomView";
        public static final String SHOW_MEETING_INFO     = "showMeetingInfo";
        public static final String SHOW_USER_LIST        = "showUserList";
        public static final String SHOW_APPLY_LIST       = "showApplyList";
        public static final String SHOW_QRCODE_VIEW      = "showQRCodeView";
        public static final String SHOW_INVITE_VIEW      = "showInviteView";

        public static final String OWNER_EXIT_ROOM_ACTION = "OWNER_EXIT_ROOM_ACTION";

        public static final String ENTER_FLOAT_WINDOW = "ENTER_FLOAT_WINDOW";
        public static final String EXIT_FLOAT_WINDOW  = "EXIT_FLOAT_WINDOW";

        public static final String SEND_IM_MSG_COMPLETE = "SEND_IM_MSG_COMPLETE";

        public static final String BAR_SHOW_TIME_RECOUNT = "BAR_SHOW_TIME_RECOUNT";
    }

    /**
     * engine事件统一回调
     */
    public interface RoomEngineEventResponder {
        void onEngineEvent(RoomEngineEvent event, Map<String, Object> params);
    }

    /**
     * UI事件统一回调
     */
    public interface RoomKitUIEventResponder {
        void onNotifyUIEvent(String key, Map<String, Object> params);
    }

    public static RoomEventCenter getInstance() {
        return RoomEventCenter.LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final RoomEventCenter INSTANCE = new RoomEventCenter();
    }

    private RoomEventCenter() {
        mEngineResponderMap = new ConcurrentHashMap<>();
        mUIEventResponderMap = new ConcurrentHashMap<>();
    }

    /**
     * 订阅engine事件
     */
    public void subscribeEngine(RoomEngineEvent event, RoomEngineEventResponder observer) {
        if (event == null || observer == null) {
            return;
        }
        if (mEngineResponderMap.containsKey(event)) {
            mEngineResponderMap.get(event).add(observer);
        } else {
            List<RoomEngineEventResponder> list = new CopyOnWriteArrayList<>();
            list.add(observer);
            mEngineResponderMap.put(event, list);
        }
    }

    /**
     * 取消订阅engine事件
     */
    public void unsubscribeEngine(RoomEngineEvent event, RoomEngineEventResponder observer) {
        if (observer == null) {
            return;
        }
        List<RoomEngineEventResponder> list = mEngineResponderMap.get(event);
        if (list == null) {
            return;
        }
        list.remove(observer);
    }

    /**
     * 订阅UI事件
     */
    public void subscribeUIEvent(String event, RoomKitUIEventResponder responder) {
        if (TextUtils.isEmpty(event) || responder == null) {
            return;
        }
        TUINotificationAdapter adapter = new TUINotificationAdapter(responder);
        if (mUIEventResponderMap.containsKey(event)) {
            mUIEventResponderMap.get(event).add(adapter);
        } else {
            List<TUINotificationAdapter> list = new CopyOnWriteArrayList<>();
            list.add(adapter);
            mUIEventResponderMap.put(event, list);
        }
        TUICore.registerEvent(RoomKitUIEvent.ROOM_KIT_EVENT, event, adapter);
    }

    /**
     * 发送UI事件
     */
    public void notifyUIEvent(String event, Map<String, Object> params) {
        TUICore.notifyEvent(RoomKitUIEvent.ROOM_KIT_EVENT, event, params);
    }

    /**
     * 取消订阅UI事件
     */
    public void unsubscribeUIEvent(String event, RoomKitUIEventResponder responder) {
        if (TextUtils.isEmpty(event) || responder == null) {
            return;
        }
        List<TUINotificationAdapter> list = mUIEventResponderMap.get(event);
        if (list == null) {
            return;
        }
        list.remove(responder);
        TUINotificationAdapter notificationAdapter = null;
        Iterator<TUINotificationAdapter> iterator = list.iterator();
        while (iterator.hasNext()) {
            TUINotificationAdapter adapter = iterator.next();
            if (adapter != null && responder.equals(adapter.mResponder)) {
                notificationAdapter = adapter;
                break;
            }
        }
        TUICore.unRegisterEvent(RoomKitUIEvent.ROOM_KIT_EVENT, event, notificationAdapter);
    }

    public void notifyEngineEvent(RoomEngineEvent event, Map<String, Object> params) {
        List<RoomEngineEventResponder> list = mEngineResponderMap.get(event);
        if (list == null) {
            return;
        }
        for (RoomEngineEventResponder roomEngineEventResponder : list) {
            roomEngineEventResponder.onEngineEvent(event, params);
            Log.d(TAG, "onEngineEvent : " + event);
        }
    }

    static class TUINotificationAdapter implements ITUINotification {
        private final RoomKitUIEventResponder mResponder;

        public TUINotificationAdapter(RoomKitUIEventResponder responder) {
            mResponder = responder;
        }

        @Override
        public void onNotifyEvent(String key, String subKey, Map<String, Object> param) {
            mResponder.onNotifyUIEvent(subKey, param);
        }
    }
}