/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow strict-local
 */

import type {DimensionValue} from '../../../../../Libraries/StyleSheet/StyleSheetTypes';
import type {ViewProps} from '../../../../../Libraries/Components/View/ViewPropTypes';
import codegenNativeComponent from '../../../../../Libraries/Utilities/codegenNativeComponent';
import type {HostComponent} from '../../../../../Libraries/Renderer/shims/ReactNativeTypes';

type NativeProps = $ReadOnly<{|
  ...ViewProps,

  // Props
  marginBack?: DimensionValue,
|}>;

export default (codegenNativeComponent<NativeProps>(
  'DimensionPropNativeComponentView',
): HostComponent<NativeProps>);
