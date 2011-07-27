/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.ui;

import static com.google.inject.name.Names.named;
import static org.eclipse.ui.PlatformUI.isWorkbenchRunning;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.ui.LanguageSpecific;
import org.eclipse.xtext.ui.editor.IURIEditorOpener;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;
import org.eclipse.xtext.ui.editor.outline.actions.IOutlineContribution;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import com.google.eclipse.protobuf.scoping.IFileUriResolver;
import com.google.eclipse.protobuf.ui.builder.AutoAddNatureEditorCallback;
import com.google.eclipse.protobuf.ui.documentation.ProtobufDocumentationProvider;
import com.google.eclipse.protobuf.ui.editor.ProtobufUriEditorOpener;
import com.google.eclipse.protobuf.ui.editor.hyperlinking.ProtobufHyperlinkDetector;
import com.google.eclipse.protobuf.ui.editor.model.ProtobufDocumentProvider;
import com.google.eclipse.protobuf.ui.editor.syntaxcoloring.HighlightingConfiguration;
import com.google.eclipse.protobuf.ui.editor.syntaxcoloring.ProtobufSemanticHighlightingCalculator;
import com.google.eclipse.protobuf.ui.internal.ProtobufActivator;
import com.google.eclipse.protobuf.ui.outline.LinkWithEditor;
import com.google.eclipse.protobuf.ui.outline.ProtobufOutlinePage;
import com.google.eclipse.protobuf.ui.preferences.PreferenceStoreAccess;
import com.google.eclipse.protobuf.ui.preferences.pages.compiler.CompilerPreferenceStoreInitializer;
import com.google.eclipse.protobuf.ui.preferences.pages.editor.save.SaveActionsPreferenceStoreInitializer;
import com.google.eclipse.protobuf.ui.preferences.pages.general.GeneralPreferenceStoreInitializer;
import com.google.eclipse.protobuf.ui.preferences.pages.paths.PathsPreferenceStoreInitializer;
import com.google.eclipse.protobuf.ui.scoping.FileUriResolver;
import com.google.eclipse.protobuf.ui.validation.ValidateOnActivation;
import com.google.inject.Binder;

/**
 * Use this class to register components to be used within the IDE.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProtobufUiModule extends AbstractProtobufUiModule {

  public static final String PLUGIN_ID = "com.google.eclipse.protobuf.ui";

  public ProtobufUiModule(AbstractUIPlugin plugin) {
    super(plugin);
    setValidationTrigger(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), plugin);
  }

  private void setValidationTrigger(IWorkbenchWindow w, AbstractUIPlugin plugin) {
    if (w == null || !(plugin instanceof ProtobufActivator)) return;
    w.getPartService().addPartListener(new ValidateOnActivation());
  }

  @Override public Class<? extends IContentOutlinePage> bindIContentOutlinePage() {
    return ProtobufOutlinePage.class;
  }

  @Override public Class<? extends IHyperlinkDetector> bindIHyperlinkDetector() {
    return ProtobufHyperlinkDetector.class;
  }

  @Override public Class<? extends IXtextEditorCallback> bindIXtextEditorCallback() {
    return AutoAddNatureEditorCallback.class;
  }

  /** {@inheritDoc} */
  @Override public void configureToggleLinkWithEditorOutlineContribution(Binder binder) {
    binder.bind(IOutlineContribution.class)
          .annotatedWith(IOutlineContribution.LinkWithEditor.class)
          .to(LinkWithEditor.class);
  }

  public void configureGeneralSettingsPreferencesInitializer(Binder binder) {
    configurePreferenceInitializer(binder, "generalPreferences", GeneralPreferenceStoreInitializer.class);
  }

  public void configureCompilerPreferencesInitializer(Binder binder) {
    configurePreferenceInitializer(binder, "compilerPreferences", CompilerPreferenceStoreInitializer.class);
  }

  public void configurePathsPreferencesInitializer(Binder binder) {
    configurePreferenceInitializer(binder, "pathsPreferences", PathsPreferenceStoreInitializer.class);
  }

  public void configureSaveActionsPreferencesInitializer(Binder binder) {
    configurePreferenceInitializer(binder, "saveActionsPreferences", SaveActionsPreferenceStoreInitializer.class);
  }

  private void configurePreferenceInitializer(Binder binder, String name,
      Class<? extends IPreferenceStoreInitializer> initializerType) {
    binder.bind(IPreferenceStoreInitializer.class).annotatedWith(named(name)).to(initializerType);
  }

  public void configureFileUriResolver(Binder binder) {
    binder.bind(IFileUriResolver.class).to(FileUriResolver.class);
  }

  public void configureDocumentProvider(Binder binder) {
    binder.bind(XtextDocumentProvider.class).to(ProtobufDocumentProvider.class);
  }

  @Override public void configureLanguageSpecificURIEditorOpener(Binder binder) {
    if (!isWorkbenchRunning())return;
    binder.bind(IURIEditorOpener.class)
          .annotatedWith(LanguageSpecific.class)
          .to(ProtobufUriEditorOpener.class);
  }

  public void configureSemanticHighlightingCalculator(Binder binder) {
    binder.bind(ISemanticHighlightingCalculator.class).to(ProtobufSemanticHighlightingCalculator.class);
  }

  public void configurePreferenceStoreAccess(Binder binder) {
    binder.bind(IPreferenceStoreAccess.class).to(PreferenceStoreAccess.class);
  }

  public void configureHighlightingConfiguration(Binder binder) {
    binder.bind(IHighlightingConfiguration.class).to(HighlightingConfiguration.class);
  }

  public void configureEObjectDocumentationProvider(Binder binder) {
    binder.bind(IEObjectDocumentationProvider.class).to(ProtobufDocumentationProvider.class);
  }
}
