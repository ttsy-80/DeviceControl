package com.devicecontrol.engine.ui.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001BC\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012$\u0010\u0006\u001a \u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\u0007\u00a2\u0006\u0002\u0010\fJ\u0012\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J\b\u0010\u0017\u001a\u00020\u000bH\u0016J\b\u0010\u0018\u001a\u00020\u000bH\u0002R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\u00020\u000e8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0012R,\u0010\u0006\u001a \u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/devicecontrol/engine/ui/model/ConfigItemDialog;", "Landroidx/fragment/app/DialogFragment;", "modelId", "", "existingConfigItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "onConfirm", "Lkotlin/Function4;", "", "", "", "", "(Ljava/lang/Long;Lcom/devicecontrol/engine/data/model/ConfigItem;Lkotlin/jvm/functions/Function4;)V", "_binding", "Lcom/devicecontrol/engine/databinding/DialogConfigItemBinding;", "binding", "getBinding", "()Lcom/devicecontrol/engine/databinding/DialogConfigItemBinding;", "Ljava/lang/Long;", "onCreateDialog", "Landroid/app/Dialog;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "setupImeActions", "engine-lib_debug"})
public final class ConfigItemDialog extends androidx.fragment.app.DialogFragment {
    @org.jetbrains.annotations.Nullable
    private final java.lang.Long modelId = null;
    @org.jetbrains.annotations.Nullable
    private final com.devicecontrol.engine.data.model.ConfigItem existingConfigItem = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function4<java.lang.Double, java.lang.String, java.lang.Integer, java.lang.Integer, kotlin.Unit> onConfirm = null;
    @org.jetbrains.annotations.Nullable
    private com.devicecontrol.engine.databinding.DialogConfigItemBinding _binding;
    
    public ConfigItemDialog(@org.jetbrains.annotations.Nullable
    java.lang.Long modelId, @org.jetbrains.annotations.Nullable
    com.devicecontrol.engine.data.model.ConfigItem existingConfigItem, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function4<? super java.lang.Double, ? super java.lang.String, ? super java.lang.Integer, ? super java.lang.Integer, kotlin.Unit> onConfirm) {
        super();
    }
    
    private final com.devicecontrol.engine.databinding.DialogConfigItemBinding getBinding() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public android.app.Dialog onCreateDialog(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    private final void setupImeActions() {
    }
    
    @java.lang.Override
    public void onDestroyView() {
    }
}