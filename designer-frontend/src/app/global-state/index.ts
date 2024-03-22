import { createContext } from "react";

export type User = {
  name: string;
  email: string;
};

export type GlobalContextValue = {
  title: string;
  breadcrumbs?: string; // e.g. Designer > Try-on
  currentUser?: User;
};

export enum GlobalContextActionType {
  SET_TITLE = "SET_TITLE",
  SET_CURRENT_USER = "SET_CURRENT_USER",
  SET_BREADCRUMBS = "SET_BREADCRUMBS",
}

export type GlobalContextReducer = (
  state: GlobalContextValue,
  action: GlobalDispatchPayload
) => GlobalContextValue;

export type GlobalContext = {
  value: GlobalContextValue;
  reducer: GlobalContextReducer;
};

export type GlobalDispatchPayload = {
  type: GlobalContextActionType;
  value: Partial<GlobalContextValue>;
};

export const defaultGlobalState: GlobalContextValue = {
  title: "Garmento | A virtual try-on platform for businesses",
};

export const globalReducer: GlobalContextReducer = (state, action) => {
  switch (action.type) {
    case GlobalContextActionType.SET_TITLE:
      return { ...state, title: action.value.title! };
    case GlobalContextActionType.SET_BREADCRUMBS:
      return { ...state, breadcrumbs: action.value.breadcrumbs };
    case GlobalContextActionType.SET_CURRENT_USER:
      return { ...state, currentUser: action.value.currentUser };
    default:
      break;
  }
  return state;
};

export const GlobalContext = createContext<GlobalContextValue | null>(null);
export const GlobalDispatchContext = createContext<
  ((value: GlobalDispatchPayload) => void) | null
>(null);
