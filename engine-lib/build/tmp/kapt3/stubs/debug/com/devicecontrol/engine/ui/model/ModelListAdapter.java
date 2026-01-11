package com.devicecontrol.engine.ui.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u001b2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002\u001b\u001cBA\u0012\u0012\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\u0004\u0012\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004\u00a2\u0006\u0002\u0010\nJ\b\u0010\u0010\u001a\u00020\u000fH\u0016J\u0010\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u000fH\u0016J\u0018\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00022\u0006\u0010\u0012\u001a\u00020\u000fH\u0016J\u0018\u0010\u0015\u001a\u00020\u00022\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u000fH\u0016J\u0014\u0010\u0019\u001a\u00020\u00062\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\fR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/devicecontrol/engine/ui/model/ModelListAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/devicecontrol/engine/ui/model/ModelListAdapter$ViewHolder;", "onAddConfigItem", "Lkotlin/Function1;", "", "", "onDeleteConfigItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "onDeleteModel", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "models", "", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "totalItems", "", "getItemCount", "getItemViewType", "position", "onBindViewHolder", "holder", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "submitList", "newModels", "Companion", "ViewHolder", "engine-lib_debug"})
public final class ModelListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.devicecontrol.engine.ui.model.ModelListAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<java.lang.Long, kotlin.Unit> onAddConfigItem = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<com.devicecontrol.engine.data.model.ConfigItem, kotlin.Unit> onDeleteConfigItem = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<java.lang.Long, kotlin.Unit> onDeleteModel = null;
    @org.jetbrains.annotations.NotNull
    private java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems> models;
    private int totalItems = 0;
    private static final int VIEW_TYPE_FIRST_ITEM = 0;
    private static final int VIEW_TYPE_NORMAL_ITEM = 1;
    @org.jetbrains.annotations.NotNull
    public static final com.devicecontrol.engine.ui.model.ModelListAdapter.Companion Companion = null;
    
    public ModelListAdapter(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onAddConfigItem, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.devicecontrol.engine.data.model.ConfigItem, kotlin.Unit> onDeleteConfigItem, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onDeleteModel) {
        super();
    }
    
    public final void submitList(@org.jetbrains.annotations.NotNull
    java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems> newModels) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override
    public int getItemViewType(int position) {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.devicecontrol.engine.ui.model.ModelListAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.ui.model.ModelListAdapter.ViewHolder holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/devicecontrol/engine/ui/model/ModelListAdapter$Companion;", "", "()V", "VIEW_TYPE_FIRST_ITEM", "", "VIEW_TYPE_NORMAL_ITEM", "engine-lib_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004Jb\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u000f0\u00192\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u000f0\u00192\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u000f0\u0019R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/devicecontrol/engine/ui/model/ModelListAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Landroid/view/View;)V", "btnAddConfigItem", "Landroid/widget/Button;", "btnDelete", "tvBladeCount", "Landroid/widget/TextView;", "tvGearRatio", "tvJogCount", "tvModelName", "tvPosition", "bind", "", "model", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "configItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "isFirstItem", "", "spanCount", "", "onAddConfigItem", "Lkotlin/Function1;", "", "onDeleteConfigItem", "onDeleteModel", "engine-lib_debug"})
    public static final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvModelName = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvGearRatio = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvPosition = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvBladeCount = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView tvJogCount = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.Button btnAddConfigItem = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.Button btnDelete = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull
        com.devicecontrol.engine.data.model.EngineModelWithConfigItems model, @org.jetbrains.annotations.NotNull
        com.devicecontrol.engine.data.model.ConfigItem configItem, boolean isFirstItem, int spanCount, @org.jetbrains.annotations.NotNull
        kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onAddConfigItem, @org.jetbrains.annotations.NotNull
        kotlin.jvm.functions.Function1<? super com.devicecontrol.engine.data.model.ConfigItem, kotlin.Unit> onDeleteConfigItem, @org.jetbrains.annotations.NotNull
        kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onDeleteModel) {
        }
    }
}