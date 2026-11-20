# Prompt Tạo Giao Diện Mobile App Trên Google Stitch (FallAlert & Health Monitor)

Dưới đây là 2 phiên bản Prompt (Bản Tiếng Anh chuẩn kỹ thuật giúp **Google Stitch** sinh UI đẹp nhất với ngôn ngữ hiển thị trên App vẫn là **Tiếng Việt**, và Bản Tiếng Việt). Bạn chỉ cần copy nguyên khối bên dưới dán vào **Stitch**.

---

## 🌟 BẢN 1: PROMPT TỐI ƯU NHẤT CHO STITCH (Khuyên dùng - Chuẩn Design System & Micro-Animations)

```text
Design a production-grade, high-craft Android Mobile App UI (Material 3 Expressive, inspired by Apple Health, Whoop, and Google Fit) for an IoT Wearable Fall Detection & Real-time Health Monitoring System connected to an "ESP32-C3 SuperMini" smart wristband. All user-facing UI text must be in Vietnamese.

### 1. VISUAL DIRECTION & DESIGN SYSTEM (NO GENERIC AI AESTHETIC)
- Theme: High-contrast Dark Clinical Tech theme (Deep Obsidian surface `#0B0F17`, Elevated Card `#151C28`, Subtle 1px border `#232E42`) with crisp typography (Inter / Plus Jakarta Sans + JetBrains Mono for live sensor numbers, `tabular-nums`). Avoid generic purple gradients.
- Semantic Status Palette (Always pair color with icons and status badges for WCAG AA accessibility):
  * Safe / Normal: Emerald (`#10B981`, soft glow `#10B981/15`)
  * Fall Emergency (Critical): Crimson Red (`#EF4444`, pulsing radar ring `#EF4444/25`)
  * False-Alarm Filtered / Warning: Amber (`#F59E0B`, `#F59E0B/15`)
  * Vital Sign / SpO2 Accent: Medical Cyan (`#06B6D4`) & Coral Rose (`#F43F5E`)
- Micro-interactions & Button Animations:
  * Tactile button press effect (`active:scale-95`, smooth cubic-bezier spring transition 200ms).
  * Subtle shimmer/ripple effect on primary action buttons.
  * Live breathing pulse ring around the wearable connection status and heart icon synced to BPM.
  * Smooth sliding pill indicator on the Bottom Navigation Bar.

### 2. CORE LAYOUT & 3-TAB NAVIGATION (MOBILE VIEWPORT 390x844)
Create a cohesive mobile app interface with a Top Device Header, a Floating Emergency Heads-Up Modal state, 3 switchable Views (Trang chủ, Lịch sử, Cài đặt), and a Collapsible "ESP32-C3 Simulator Drawer/Toolbar" for live demo testing.

#### A. Top App Bar (Persistent)
- App Brand: "FallGuard C3" with a wrist-sensor icon.
- Live Connection Pill: Green pulsing dot "ESP32-C3 Online" + sub-badge "⌚ Đang đeo tay" + live timestamp.

#### B. Screen 1: TRANG CHỦ (Live Health & Fall Dashboard)
1. Hero Dynamic Status Card (Supports 3 visual states with smooth transitions):
   - State 1 (An Toàn): Emerald border & icon, "✅ TRẠNG THÁI: AN TOÀN", subtitle "Hoạt động: Đi bộ bình thường • Không có va chạm".
   - State 2 (Đã Chặn Báo Giả - Hard Negative ADL): Amber border, "🛡️ ĐÃ LỌC BÁO ĐỘNG GIẢ", subtitle "Vung tay mạnh / Ngồi phịch xuống ghế • Lực hoặc AI chưa vượt ngưỡng kép".
   - State 3 (Cảnh Báo Té Ngã Khẩn Cấp): Crimson pulsing card with animated radar waves, "🚨 PHÁT HIỆN TÉ NGÃ: NGÃ NGỬA ĐẬP SÀN", displaying circular SVG 15-second countdown timer ("Tự động gọi SOS sau 15s") and two prominent tactile CTA buttons:
     * Primary White Button: "✔ Xác nhận & Tắt còi C3" (with ripple press animation)
     * Urgent Red/Dark Button: "📞 Gọi Khẩn Cấp 115" (with vibrating phone icon animation)
2. 2x2 Bento Grid of Real-time Sensor Cards (`tabular-nums` large numbers + micro trend indicator):
   - Card 1 (Nhịp tim - MAX30102): Animated beating heart icon, "78 BPM", status pill "Bình thường (60-100)".
   - Card 2 (Nồng độ Oxy - SpO2): Blood drop icon, "98 %", circular mini progress bar, status "Tốt (95-100%)".
   - Card 3 (Lực va chạm đỉnh - SMV MPU6050): Impact icon, "9.8 m/s²", horizontal progress bar showing current force vs threshold line at "16.0 m/s²".
   - Card 4 (Độ tin cậy AI TinyML): Brain/Chip icon, "2.4 % Fall", subtitle "ADL (Sinh hoạt): 97.6%".
3. Dual-Layer Fusion Verification Strip:
   - Compact horizontal card showing how the 2-layer algorithm decides:
     * Pill 1: "1. AI TinyML ≥ 75%" (Check/Cross icon)
     * Pill 2: "2. Lực đập SMV > 16.0 m/s²" (Check/Cross icon)
     * Right indicator: "🔕 Còi Buzzer C3: TẮT" (or "🔊 ĐANG HÚ" when falling).
4. Live Dual-Waveform Chart Card:
   - Smooth animated dark canvas/SVG line chart plotting real-time Acceleration Impact (SMV in m/s²) with a dashed red threshold line at 16.0 m/s², plus a toggle to view PPG Heart Rate waveform.

#### C. Screen 2: LỊCH SỬ (Incident & Alert Timeline)
- Filter chips at top: "Tất cả", "🚨 Té ngã", "🛡️ Đã lọc báo giả", "❤️ Tim mạch/SpO2" + "Xóa lịch sử" action button.
- Vertical timeline list of event cards with color-coded left accent bars, icon badges, event title (e.g., "Ngã chúi về phía trước", "Vung tay mạnh - Đã chặn báo giả"), vital snapshot at incident time ("Lực: 22.4 m/s² • AI: 94% • HR: 118 BPM • SpO2: 95%"), and exact timestamp.

#### D. Screen 3: CÀI ĐẶT & PHẦN CỨNG (Settings & Calibration)
- Interactive Slider Card: "Ngưỡng lực va chạm tiếp đất (SMV)" adjustable from 12.0 to 22.0 m/s² (default 16.0 m/s²) with live numeric badge.
- Emergency Contact Input Card: "Số điện thoại SOS Khẩn cấp" (`0912.345.678 - Người thân`) with quick-test call button.
- Remote Hardware Diagnostic Card: Tactile button "🔊 Bấm kêu thử còi Buzzer trên ESP32-C3" with animated soundwave feedback when clicked, plus MQTT Broker status (`broker.emqx.io:1883` • Topic: `fall_alert/c3_wrist/telemetry`).

#### E. Interactive Demo Simulator Bar (Floating or Bottom/Side Dock for Testing)
- Include a sleek segmented control bar allowing the user to click and test dummy scenarios with instant UI state & chart animations:
  * "🎲 Ngẫu nhiên" (Random scenario)
  * "🚨 Giả lập Ngã thật" (Cycles through Backward fall, Forward fall, Lateral fall, Syncope collapse, Chair slip)
  * "🏃 Giả lập Báo giả" (Abrupt sit, Wrist swing)
  * "❤️ Bất thường Tim/SpO2" (Tachycardia 138 BPM / Hypoxia 89% SpO2)
  * "✅ Bình thường" (Reset to safe state)
```

---

## 🇻🇳 BẢN 2: PROMPT TIẾNG VIỆT (Nếu bạn muốn nhập hoàn toàn bằng Tiếng Việt vào Stitch)

```text
Hãy thiết kế giao diện Mobile App Android cao cấp (phong cách Material 3 Expressive kết hợp Apple Health / Whoop, giao diện Dark Mode y tế hiện đại, độ tương phản cao, chuẩn WCAG AA) cho hệ thống "Cảnh báo Té ngã & Theo dõi Sức khỏe Thời gian thực" kết nối với vòng đeo cổ tay thông minh ESP32-C3 SuperMini.

1. PHONG CÁCH THIẾT KẾ & HIỆU ỨNG ANIMATION:
- Tuyệt đối không dùng màu tím/indigo kiểu AI đại trà. Sử dụng nền tối chuẩn thiết bị y tế (#0B0F17), thẻ nổi (#151C28), viền mảnh tinh tế (#232E42).
- Màu sắc theo ngữ nghĩa rõ ràng kèm icon:
  + An toàn: Xanh ngọc lục bảo (Emerald #10B981)
  + Té ngã khẩn cấp: Đỏ thẫm (Crimson #EF4444) kèm hiệu ứng vòng sóng radar lan tỏa (pulsing ripple)
  + Đã chặn báo động giả (ADL): Vàng hổ phách (Amber #F59E0B)
  + Chỉ số nhịp tim / SpO2: Đỏ Coral (#F43F5E) và Xanh Cyan (#06B6D4)
- Hiệu ứng nút bấm & tương tác:
  + Mọi nút bấm đều có hiệu ứng đàn hồi (spring scale-95 khi nhấn, hover sáng nhẹ, hiệu ứng gợn sóng ripple).
  + Icon trái tim đập theo nhịp BPM thực tế.
  + Các con số cảm biến dùng font monospace (tabular-nums) nhảy số mượt mà.

2. BỐ CỤC MÀN HÌNH ĐIỆN THOẠI (3 TAB CHÍNH + THANH GIẢ LẬP DỮ LIỆU DUMMY):
- Thanh Header trên cùng: Tên app "FallGuard C3", chấm xanh nhấp nháy "ESP32-C3 Online", huy hiệu "⌚ Đang đeo tay" và đồng hồ thời gian thực.

- TAB 1: TRANG CHỦ (Dashboard)
  + Banner Trạng thái Động (hỗ trợ 3 trạng thái: An toàn [Xanh], Đã lọc báo giả khi vung tay/ngồi mạnh [Vàng], và Cảnh báo Té ngã Khẩn cấp [Đỏ nhấp nháy]).
  + Khi ở trạng thái Té ngã: Bung hộp thoại khẩn cấp nổi bật với vòng tròn đếm ngược 15 giây ("Tự gọi SOS sau 15s") và 2 nút bấm lớn có hiệu ứng động: "✔ Xác nhận & Tắt còi C3" và "📞 Gọi Khẩn Cấp 115".
  + Lưới Bento 2x2 gồm 4 thẻ thông số trực tuyến:
    1. ❤️ Nhịp tim (BPM - cảm biến MAX30102)
    2. 🩸 Nồng độ Oxy máu SpO2 (%)
    3. 💥 Lực đập đỉnh SMV (m/s² - cảm biến MPU6050 kèm thanh tiến trình so với ngưỡng 16.0 m/s²)
    4. 🤖 AI TinyML dự đoán (% Fall vs % ADL)
  + Thanh kiểm chứng Bộ lọc 2 tầng: Hiển thị rõ điều kiện (1. AI Fall >= 75%) + (2. Lực SMV > 16.0 m/s²) và trạng thái Còi Buzzer trên mạch C3 (TẮT / ĐANG HÚ).
  + Biểu đồ đường (Live Waveform Chart) vẽ xung lực gia tốc SMV theo thời gian thực có đường kẻ đứt nét màu đỏ tại ngưỡng 16.0 m/s².

- TAB 2: LỊCH SỬ (History Timeline)
  + Bộ lọc nhanh (Tất cả / Té ngã / Đã lọc báo giả / Bất thường tim mạch) và nút "Xóa lịch sử".
  + Danh sách dòng thời gian các sự kiện kèm đầy đủ thông số lúc xảy ra sự cố (Loại ngã, Lực đập m/s², AI %, Nhịp tim, SpO2, Thời gian).

- TAB 3: CÀI ĐẶT (Settings)
  + Thanh trượt (Slider) chỉnh ngưỡng lực va chạm tiếp đất SMV từ 12.0 đến 22.0 m/s² (mặc định 16.0 m/s²).
  + Ô nhập số điện thoại SOS người thân.
  + Nút bấm có hiệu ứng sóng âm "🔊 Bấm kêu thử còi Buzzer C3 từ xa" và thông tin kết nối MQTT.

- THANH CÔNG CỤ TEST DỮ LIỆU DUMMY (Simulator Dock):
  + Có 5 nút bấm đẹp mắt để test trực tiếp các kịch bản trên giao diện: "🎲 Random Ngẫu nhiên", "🚨 Giả lập Ngã thật (5 kiểu ngã)", "🏃 Giả lập Báo giả (Vung tay/Ngồi mạnh)", "❤️ Bất thường Tim/SpO2", "✅ Trở về Bình thường".
```
