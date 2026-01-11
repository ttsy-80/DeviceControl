package com.devicecontrol.engine.ui.task;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010#\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0017B-\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0004\u0012\u0004\u0012\u00020\t0\u0007\u00a2\u0006\u0002\u0010\nJ\b\u0010\r\u001a\u00020\u000eH\u0016J\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u0004J\u001c\u0010\u0010\u001a\u00020\t2\n\u0010\u0011\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0012\u001a\u00020\u000eH\u0016J\u001c\u0010\u0013\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000eH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0004\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/devicecontrol/engine/ui/task/GearRatioAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/devicecontrol/engine/ui/task/GearRatioAdapter$ViewHolder;", "configItems", "", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "onSelectionChanged", "Lkotlin/Function1;", "", "", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "selectedGearRatios", "", "getItemCount", "", "getSelectedGearRatios", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ViewHolder", "engine-lib_debug"})
public final class GearRatioAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.devicecontrol.engine.ui.task.GearRatioAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.devicecontrol.engine.data.model.ConfigItem> configItems = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<java.util.List<java.lang.Double>, kotlin.Unit> onSelectionChanged = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.Set<java.lang.Double> selectedGearRatios = null;
    
    public GearRatioAdapter(@org.jetbrains.annotations.NotNull
    java.util.List<com.devicecontrol.engine.data.model.ConfigItem> configItems, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.util.List<java.lang.Double>, kotlin.Unit> onSelectionChanged) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.devicecontrol.engine.ui.task.GearRatioAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.ui.task.GearRatioAdapter.ViewHolder holder, int position) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.Double> getSelectedGearRatios() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/devicecontrol/engine/ui/task/GearRatioAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Lcom/devicecontrol/engine/ui/task/GearRatioAdapter;Landroid/view/View;)V", "cbGearRatio", "Landroid/widget/CheckBox;", "tvGearRatioInfo", "Landroid/widget/TextView;", "bind", "", "configItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "isSelected", "", "engine-lib_debug"})
    public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final android.widget.CheckBox cbGearRatio = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvGearRatioInfo = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull
        com.devicecontrol.engine.data.model.ConfigItem configItem, boolean isSelected) {
        }
    }
}